package com.hazelcast.samples.json.jsongrid;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>The {@code JdbcTemplate} injected by Spring gives us
 * a connection to the database, based on the
 * {@code application.yml} file.
 * </p>
 * <p>Run two queries against the "{@code potus}" table
 * holding the <b>P</b>residents <b>O</b>f <b>T</b>he
 * <b>U</b>nited <b>S</b>tates. The "{@code vpotus}" table
 * is the same idea for the <b>V</b>ice president.
 * </p>
 * <p>These two tables are deliberately coded to have
 * different columns, to demonstrate the code is working
 * correctly. It would make much more sense for them
 * to be the same or somehow combined.
 * </p>
 * <p>So long as some rows are displayed, everything is
 * working fine. At the time of Hazelcast 3.12's launch
 * of enhanced JSON support there had been 45 presidents
 * and 48 vice-presidents.
 * </p>
 */
@Configuration
@Slf4j
public class ApplicationRunner implements CommandLineRunner {

    private static final String[] SQLS = {
            "SELECT * FROM potus",
            "SELECT * FROM vpotus",
            "SELECT COUNT(*) FROM potus",
            "SELECT COUNT(*) FROM vpotus",
    };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        for (String sql : SQLS) {
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println(sql);

            try {
                List<Map<String, Object>> resultSet = this.jdbcTemplate.queryForList(sql);

                for (int i = 0; i < resultSet.size(); i++) {
                    Map<String, Object> map = resultSet.get(i);

                    StringBuilder stringBuilder = new StringBuilder();

                    stringBuilder.append((i + 1) + ":");

                    Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
                    int j = 0;
                    while (iterator.hasNext()) {
                        Map.Entry<String, Object> entry = iterator.next();
                        if (j > 0) {
                            stringBuilder.append(",");
                        }
                        stringBuilder.append(entry.getKey() + "==" + entry.getValue());
                        j++;
                    }
                    System.out.println(stringBuilder.toString());
                }
                System.out.printf("[%d row%s]%n", resultSet.size(), (resultSet.size() == 1 ? "" : "s"));

            } catch (Exception e) {
                log.error(sql, e);
            }

            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
    }
}
