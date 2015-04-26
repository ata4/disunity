/*
 ** 2015 April 26
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package org.json;

import java.io.Writer;

/**
 * Small extension for JSONObject to allow pretty printing.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class JSONObject2 extends JSONObject {

    public Writer write(Writer writer, int indentFactor) throws JSONException {
        return write(writer, indentFactor, 0);
    }
}
