package com.liquidhub.framework.ci.logger

class PrintStreamLogger implements Logger{

	final PrintStream out
	boolean debug = true

	PrintStreamLogger(OutputStream out) {
		this.out = new PrintStream(out)
	}

	@Override
	void error(msg) {
		use(ANSIColoringCategory) {
			out.println containsColor(msg) ? msg : msg.error()
		}
	}

	@Override
	void warn(msg) {
		use(ANSIColoringCategory) {
			out.println containsColor(msg) ? msg : msg.warn()
		}
	}

	@Override
	void info(msg) {
		use(ANSIColoringCategory) {
			out.println containsColor(msg) ? msg : msg.info()
		}
	}


	void trace(msg) {
		use(ANSIColoringCategory) {
			out.println containsColor(msg) ? msg : msg.note()
		}
	}

	@Override
	void debug(msg) {
		if (isDebug()) {
			out.println msg
		}
	}

	private boolean containsColor(msg) {
		(msg ==~ /(?m)^.*\x1B\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K].*$/)
	}
}
