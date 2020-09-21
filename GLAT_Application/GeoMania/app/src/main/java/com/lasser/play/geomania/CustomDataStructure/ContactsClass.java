package com.lasser.play.geomania.CustomDataStructure;

/**
 * Created by niranj555 on 01-04-2017.
 */

public class ContactsClass {

    public String number;
    public String name;
   public String[] Strlist;
    public String[] toStringContents()
    {
        Strlist=new String[2];

        Strlist[0]=number;
        Strlist[1]=name;

        return Strlist;
    }

    public String getName()
    {  return name;}

    public String getNumber()
    {return number;}


    @Override
    public String toString()
    {
        return "{'"+getName()+"':'"+getNumber()+"'}";
    }


}
