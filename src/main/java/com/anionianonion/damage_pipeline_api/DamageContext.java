package com.anionianonion.damage_pipeline_api;

import com.anionianonion.advanced_arpg_attributes_api.api.AdvancedARPGAttributesAPI;
import com.anionianonion.damage_pipeline_api.api.DamagePipelineAPI;
import com.anionianonion.elementals_api.api.ElementalsAPI;

import java.util.HashSet;
import java.util.List;

public class DamageContext {

    private HashSet<String> tags = new HashSet<>();
    private String element;
    private String source = "self";
    private float projectileSpeed;

    //
    private final ElementalsAPI elementalAPI = new ElementalsAPI();

    public DamageContext() {
        addTag("damage");
    }

    //this was the main constructor, but I decided it was beneficial in the damage capability provider to store a damage context without any args, and then set it later
    public DamageContext(String element, String... damageTags) {
        HashSet<String> unchecked = new HashSet<>(List.of(damageTags));
        HashSet<String> finalSet = new HashSet<>();

        if(elementalAPI.getAllElements().contains(element)) {
            finalSet.add(element);
            this.element = element;
        }

        //check only tags and adds non-elements, because we already checked elements in the if statement above
        unchecked.forEach(tag -> {
            if(AdvancedARPGAttributesAPI.getValidTags().contains(tag) && !elementalAPI.getAllElements().contains(tag)) finalSet.add(tag);
        });

        this.tags = finalSet;

        addTag("damage");
    }
    public HashSet<String> getTags() {
        return this.tags;
    }

    public void setTags(String... damageTags) {
        HashSet<String> unchecked = new HashSet<>(List.of(damageTags));
        HashSet<String> finalSet = new HashSet<>();

        //automatically adds the element if it's valid, and damageTags doesn't contain it.
        if(elementalAPI.getAllElements().contains(element) && !unchecked.contains(element)) {
            finalSet.add(element);
        }

        if(DamagePipelineAPI.getValidDamageSourceTypeTags().contains(source) && !unchecked.contains(source)) {
            finalSet.add(source);
        }

        //check only tags and adds non-elements, because we already checked elements in the if statement above
        unchecked.forEach(tag -> {
            if(AdvancedARPGAttributesAPI.getValidTags().contains(tag) && !elementalAPI.getAllElements().contains(tag)) finalSet.add(tag);
        });

        this.tags = finalSet;

        addTag("damage");
    }

    public void clearTags() {
        this.tags.clear();
        this.tags.add("damage");
    }

    public void addTag(String damageTag) {
        if(AdvancedARPGAttributesAPI.getValidTags().contains(damageTag) && !elementalAPI.getAllElements().contains(damageTag)) this.tags.add(damageTag);
    }

    public void setProjectileSpeed(float projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
    }

    public float getProjectileSpeed() {
        return this.projectileSpeed;
    }

    public String getElement() {
        return this.element;
    }

    public void setElement(String element) {
        if(elementalAPI.getAllElements().contains(element)) {
            //add new element id to tags
            this.tags.add(element);
            //removes old element from tags
            this.tags.remove(this.element);
            //set element to new element
            this.element = element;
        }
    }

    public String getSource() { return this.source; }

    public void setSource(String source) {
        if(DamagePipelineAPI.getValidDamageSourceTypeTags().contains(source)) {
            this.tags.add(source);
            this.tags.remove(this.source);
            this.source = source;
        }
    }
}