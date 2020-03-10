package com.hazelcast.samples.querying.jet;

import com.hazelcast.jet.Util;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.function.Functions;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.JoinClause;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.samples.querying.domain.LifeValue;
import com.hazelcast.samples.querying.domain.PersonKey;
import com.hazelcast.samples.querying.domain.PersonValue;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import static com.hazelcast.jet.datamodel.Tuple3.tuple3;

/**
 * <p>
 * Construct a map by streaming and joining (in memory) the contents of two
 * other maps. What we are trying to do is join the contents of the
 * "{@code person}" map with the "{@code deaths}" map.
 * </P>
 * <p>
 * In relational database terms, it would look a bit like this.
 * </P>
 *
 * <PRE>
 * SELECT firstName, dateOfBirth, dateOfDeath
 * FROM person, deaths
 * WHERE person.firstName = deaths.key
 * </PRE>
 * <p>
 * In Jet, it looks more like this.
 * </P>
 *
 * <PRE>
 * +----------+                +----------+
 * |1 "Person"|                |3 "Deaths"|
 * |  IMap    |                |  IMap    |
 * +----------+                +----------+
 * |                            |
 * |                            |
 * +----------+                +----------+
 * |3 "Person"|                |4 "Deaths"|
 * | to tuple |                | to tuple |
 * +----------+                +----------+
 * \              /
 * \            /
 * +------------+
 * |5  Join     |
 * |on firstName|
 * +------------+
 * |
 * |
 * +----------+
 * |6 Filter  |
 * | unmatched|
 * +----------+
 * |
 * |
 * +----------+
 * |7 Convert |
 * | to Entry |
 * +----------+
 * |
 * |
 * +----------+
 * |8 "Life"  |
 * |   IMap   |
 * +----------+
 * </PRE>
 * <p>
 * There are eight parts to this joining pipeline, numbered in the diagram
 * above.
 * </P>
 * <OL>
 * <LI>
 * <p>
 * <B>{@code Person} map</B> Read from {@link com.hazelcast.core.IMap IMap}
 * named "{@code person}" and stream this a series of map entries into the
 * pipeline.
 * </P>
 * </LI>
 * <LI>
 * <p>
 * <B>Reformat</B> Create a tuple of the only two fields we want from the
 * "{@code person}" map.
 * </P>
 * </LI>
 * <LI>
 * <p>
 * <B>{@code Deaths} map</B> Same as for step 1, except the name is
 * "{@code deaths}"
 * </P>
 * </LI>
 * <LI>
 * <p>
 * <B>Reformat</B> Create a tuple of the only two fields we want from the
 * "{@code deaths}" map.
 * </P>
 * </LI>
 * <LI>
 * <p>
 * <B>Join</B> Join the output of stages 2 and 4 for matching key
 * ({@code firstName}
 * </P>
 * <p>
 * The output of this stage is a pair of
 * {@code (String, LocalDate), (String, LocalDate)}
 * <p>
 * </LI>
 * <LI>
 * <p>
 * <B>Filter</B> Remove items from the join with only dates of birth, no dates
 * of death.
 * </P>
 * </LI>
 * <LI>
 * <p>
 * <B>Reformat</B> Convert the output of the previous stage into a
 * {@code String} key and pair of {@code LocalDate} for value
 * </P>
 * </LI>
 * <LI>
 * <p>
 * <B>{@code Life} map</B> Save the output from stage 4 into an
 * {@link com.hazelcast.core.IMap IMap}
 * </P>
 * </LI>
 * </OL>
 */
public class MyJoinPipeline {

    public static Pipeline build() {
        Pipeline pipeline = Pipeline.create();
        // 1 - read a map
        BatchStage<Entry<String, LocalDate>> births = pipeline
                .drawFrom(Sources.<PersonKey, PersonValue>map("person"))
                .map(entry -> Util.entry(entry.getKey().getFirstName(), entry.getValue().getDateOfBirth()));

        // 2 - read another map
        BatchStage<Entry<String, LocalDate>> deaths = pipeline.drawFrom(Sources.map("deaths"));

        // 5 - join output from steps 2 and 4 (Tuple2 are map entries) on key
        BatchStage<Tuple3<String, LocalDate, LocalDate>> stage5 = deaths.hashJoin(births,
                JoinClause.joinMapEntries(Functions.entryKey()),
                (nameAndBirth, death) -> tuple3(nameAndBirth.getKey(), nameAndBirth.getValue(), death));

        // 6 - filter out unjoined
        BatchStage<Tuple3<String, LocalDate, LocalDate>> stage6 = stage5
                .filter(tuple2 -> tuple2.f1() != null);

        // 7 - create a map entry from step 6 output
        BatchStage<SimpleImmutableEntry<String, LifeValue>> stage7 = stage6.map(trio -> {
            // Tuple2<Tuple2< key, date-of-birth>, date-of-death>
            String key = trio.f0();
            LocalDate dob = trio.f1();
            LocalDate dod = trio.f2();

            LifeValue value = new LifeValue();
            value.setDateOfBirth(dob);
            value.setDateOfDeath(dod);

            // Create a Map.Entry
            return new SimpleImmutableEntry<>(key, value);
        });

        // 8 - save the map entry
        stage7.drainTo(Sinks.map("life"));

        // Return the query execution plan
        return pipeline;
    }

}
