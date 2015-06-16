package com.liquidhub.framework.ci.logger

@Category(String)
class ANSIColoringCategory {

    String error() {
        "\033[1;31m${this}\033[0m"
    }

    String warn() {
        "\033[1;31m${this}\033[0m"
    }

    String info() {
        "\033[1;32m${this}\033[0m"
    }

    String note() {
        "\033[1;36m${this}\033[0m"
    }
}