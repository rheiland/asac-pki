package base;

public class ActorProperty extends Property {
	public String actorName;

	public ActorProperty(String[] qualifiers, String name, String[] params) {
		super(qualifiers, name, params);
	}

	public ActorProperty(Object o, String name, String[] params) {
		super(o, name, params);
	}

	public ActorProperty(Object o, String name, String[] params, String an) {
		super(o, name, params);
		actorName = an;
	}

	public ActorProperty(Object o, String name) {
		super(o, name);
	}

	public ActorProperty(Object o, String name, String an) {
		super(o, name);
		actorName = an;
	}

	public ActorProperty(Class c, String name, String[] params) {
		super(c, name, params);
	}

	public ActorProperty(Class c, String name, String[] params, String an) {
		super(c, name, params);
		actorName = an;
	}

	public ActorProperty(Class c, String name) {
		super(c, name);
	}

	public ActorProperty(Class c, String name, String an) {
		super(c, name);
		actorName = an;
	}

	public String getActorName() {
		return actorName;
	}

	public void setActorName(String an) {
		actorName = an;
	}
}

