package pki.measure;

import base.ActorProperty;

import pki.ActorAdmin;

public interface AskAdmin {
    // Common properties used in measures
    public static final ActorProperty ADD_BIAS_PROPERTY = new ActorProperty(ActorAdmin.class, "addBias", "a0");
    public static final ActorProperty UR_BIAS_PROPERTY = new ActorProperty(ActorAdmin.class, "urBias", "a0");
}

