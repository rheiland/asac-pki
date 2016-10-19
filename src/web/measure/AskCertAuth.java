package web.measure;

import base.ActorProperty;

import web.ActorCertAuth;

public interface AskCertAuth {
    // Common properties used in measures
    public static final ActorProperty ADD_BIAS_PROPERTY = new ActorProperty(ActorCertAuth.class, "addBias", "a0");
    public static final ActorProperty UR_BIAS_PROPERTY = new ActorProperty(ActorCertAuth.class, "urBias", "a0");
}

