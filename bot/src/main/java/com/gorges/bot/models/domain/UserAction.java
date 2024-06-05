package com.gorges.bot.models.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class UserAction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Command command;
    private final String specificAction;    // specific action within one command
    private final LocalDateTime createdTime = LocalDateTime.now();

    public UserAction(Command command, String specificAction) {
        this.command = command;
        this.specificAction = specificAction;
    }

    public Command getCommand() {
        return command;
    }

    public String getSpecificAction() {
        return specificAction;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAction that = (UserAction) o;
        return Objects.equals(command, that.command) &&
            Objects.equals(specificAction, that.specificAction) &&
            Objects.equals(createdTime, that.createdTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, specificAction, createdTime);
    }

    @Override
    public String toString() {
        return "UserAction [command=" + command +
            ", action=" + specificAction +
            ", createdTime=" + createdTime + "]";
    }

}
