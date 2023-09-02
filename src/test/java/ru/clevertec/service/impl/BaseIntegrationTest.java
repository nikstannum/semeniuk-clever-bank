package ru.clevertec.service.impl;

import liquibase.command.CommandScope;
import liquibase.exception.CommandExecutionException;

public class BaseIntegrationTest {
    void runMigration() {
        CommandScope updateCommand;
        try {
            CommandScope dropAll = new CommandScope("dropAll");
            dropAll.addArgumentValue("url", "jdbc:postgresql://127.0.0.1:5432/bank");
            dropAll.addArgumentValue("username", "postgres");
            dropAll.addArgumentValue("password", "root");
            dropAll.execute();
            updateCommand = new CommandScope("update");
            updateCommand.addArgumentValue("url", "jdbc:postgresql://127.0.0.1:5432/bank");
            updateCommand.addArgumentValue("username", "postgres");
            updateCommand.addArgumentValue("password", "root");
            updateCommand.addArgumentValue("changeLogFile", "db/changelog/db.changelog-master.yaml");
            updateCommand.execute();
        } catch (CommandExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
