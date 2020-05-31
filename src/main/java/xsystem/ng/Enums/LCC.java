package xsystem.ng.Enums;

import java.util.ArrayList;
import java.util.HashMap;

import xsystem.ng.Support.Config;

public class LCC implements CharClass {
    
	public String rep(){
        return "\\w";
    }

    public XClass toXClass(){
        return new X_LCC();
    }

    public ArrayList<Character> domain(){
        char[] lowerCaseChar = new Config().lowerCaseChar;
        ArrayList<Character> charlst = new ArrayList<Character>();
        for(char ch: lowerCaseChar){
            charlst.add(ch);
        }
        return charlst;
    }

    public Boolean isClass(){
        return true;
    }

    public HashMap<Character, Double> baseHist(){
        ArrayList<Character> chars = domain();
        HashMap<Character, Double> history = new HashMap<Character, Double>();
        chars.forEach(
            (c) -> history.put(c, 0.0)
        );
        return history;
    }
    
}