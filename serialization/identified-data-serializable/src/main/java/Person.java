import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import java.io.IOException;

public class Person implements IdentifiedDataSerializable {

    private String name;

    public Person() {
    }

    Person(String name) {
        this.name = name;
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.name = in.readString();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeString(name);
    }

    @Override
    public int getFactoryId() {
        return PersonDataSerializableFactory.ID;
    }

    @Override
    public int getClassId() {
        return PersonDataSerializableFactory.PERSON_TYPE;
    }

    @Override
    public String toString() {
        return String.format("Person(name=%s)", name);
    }
}
