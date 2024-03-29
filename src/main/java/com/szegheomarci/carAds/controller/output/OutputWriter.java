package com.szegheomarci.carAds.controller.output;

import com.szegheomarci.carAds.model.Car;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Map;

public abstract class OutputWriter {
    protected ArrayList<Car> results;
    protected Yaml yaml = new Yaml();
    //protected String config;
    //protected Map<String, Object> output;
    protected Map<String, Object> config;
    public OutputWriter(String outputconfig) {
        //this.config = outputconfig;
        //this.output = yaml.load(config);
        config = yaml.load(outputconfig);

    }
    public abstract void generateOutput(ArrayList<Car> resultset);


}


