package com.liquidhub.framework.ci.logger

interface Logger {

    void error(msg)

    void warn(msg)

    void info(msg)

    void trace(msg)

    void debug(msg)
}