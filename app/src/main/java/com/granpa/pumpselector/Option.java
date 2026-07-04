package com.granpa.pumpselector;
public class Option { public final String value,label,detail; public final boolean mainCategory; public Option(String v,String l){this(v,l,"",false);} public Option(String v,String l,String d){this(v,l,d,false);} public Option(String v,String l,String d,boolean m){value=v;label=l;detail=d==null?"":d;mainCategory=m;} public String toString(){return label;} }
