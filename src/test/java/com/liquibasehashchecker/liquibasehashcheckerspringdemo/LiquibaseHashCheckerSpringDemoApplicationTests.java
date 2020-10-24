package com.liquibasehashchecker.liquibasehashcheckerspringdemo;

import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Add this to the project housing the liquibase you are trying to get the hash for,
 * so that your configured springLiquibase bean (and implicitly database) is used
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LiquibaseHashCheckerSpringDemoApplicationTests {

	static {
		System.setProperty("load.db.dataset", "test");
	}

	@Autowired
	private SpringLiquibase springLiquibase;

	@Test
	public void test() throws SQLException, LiquibaseException {
		final String changelogPath = "config/liquibase/changelog/test.xml";

		// derive the database liquibase from the current configured bean specifically for contextual ChangeLogParameters
		final Connection connection = springLiquibase.getDataSource().getConnection();
		final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
		final Liquibase liquibase = new Liquibase(springLiquibase.getChangeLog().split("classpath:")[1], new ClassLoaderResourceAccessor(), database);

		final ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changelogPath, new ClassLoaderResourceAccessor());
		final DatabaseChangeLog databaseChangeLog = parser.parse(changelogPath, liquibase.getChangeLogParameters(), new ClassLoaderResourceAccessor());
		final Map<String, String> changeSetHashes = new HashMap<>();
		for (final ChangeSet changeSet : databaseChangeLog.getChangeSets()) {
			changeSetHashes.put(changeSet.getId(), changeSet.generateCheckSum().toString());
		}
		System.out.println(changeSetHashes);
	}

}