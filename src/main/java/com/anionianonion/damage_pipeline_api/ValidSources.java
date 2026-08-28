package com.anionianonion.damage_pipeline_api;

import java.util.HashSet;
import java.util.Set;

public class ValidSources {

    private static Set<String> validDamageSourceTypeTags = new HashSet<>();
    public static Set<String> getValidDamageSourceTypeTags() {
        return validDamageSourceTypeTags;
    }

    /**
    to be used for adding tags to damage, such as "self" when attacking with your own weapons/spells, "minion" when attacking with minions,
     "totem" with totems, etc.
     */
    public static void addValidDamageSourceTypeTag(String validDamageSourceTypeTag) {
        validDamageSourceTypeTags.add(validDamageSourceTypeTag);
    }
}
