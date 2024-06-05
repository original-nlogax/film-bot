package com.gorges.bot.handlers;

import com.gorges.bot.annotations.Handle;
import com.gorges.bot.models.domain.Command;

@Handle
public interface Handler {

    Command getCommand();

}
