package jplee.worldmanager.util;

import java.util.regex.Pattern;

public final class MatchPatterns {

	public static final Pattern lineFormat = Pattern
		.compile("^((?:\\w+:)?\\w+(?:\\[[\\w=*,!& ]*\\])?)((?:\\|\\w+=[\\w=*,.!& :\\[\\]]+)+)?$");
	public static final Pattern blockPattern = Pattern
		.compile("((?:\\w+:)?\\w+)(?:\\[((?:\\w+=[\\w*!& ]+,?)*|\\*)\\]+)?");
	public static final Pattern propPattern = Pattern.compile(
		"([\\w]+)=(\\d+(?:\\.\\d*)?|\\.\\d+|(?:\\w+:)?\\w+(?::\\d)* \\d*|(?:\\w+:)?\\w+(?:\\[[\\w=*!& ,]*\\])?)");
	public static final Pattern statePattern = Pattern.compile("(\\w+)=([\\w*!& ]+)");
}
