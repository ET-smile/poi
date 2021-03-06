/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hslf.record;

import org.apache.poi.ddf.*;
import org.apache.poi.util.*;

/**
 * An atom record that specifies whether a shape is a placeholder shape.
 * The number, position, and type of placeholder shapes are determined by
 * the slide layout as specified in the SlideAtom record.
 */
public class EscherPlaceholder extends EscherRecord {
    public static final short RECORD_ID = (short)RecordTypes.OEPlaceholderAtom.typeID;
    public static final String RECORD_DESCRIPTION = "msofbtClientTextboxPlaceholder";

    int position = -1;
    byte placementId = 0;
    byte size = 0;
    short unused = 0;
    
    public EscherPlaceholder() {}
    
    public int fillFields(byte[] data, int offset, EscherRecordFactory recordFactory) {
        int bytesRemaining = readHeader( data, offset );

        position = LittleEndian.getInt(data, offset+8);
        placementId = data[offset+12];
        size = data[offset+13];
        unused = LittleEndian.getShort(data, offset+14);
        
        assert(bytesRemaining + 8 == 16);
        return bytesRemaining + 8;
    }

    public int serialize(int offset, byte[] data, EscherSerializationListener listener) {
        listener.beforeRecordSerialize( offset, getRecordId(), this );
        
        LittleEndian.putShort(data, offset, getOptions());
        LittleEndian.putShort(data, offset+2, getRecordId());
        LittleEndian.putInt(data, offset+4, 8);
        LittleEndian.putInt(data, offset+8, position);
        LittleEndian.putByte(data, offset+12, placementId);
        LittleEndian.putByte(data, offset+13, size);
        LittleEndian.putShort(data, offset+14, unused);
        
        listener.afterRecordSerialize( offset+getRecordSize(), getRecordId(), getRecordSize(), this );
        return getRecordSize();
    }

    public int getRecordSize() {
        return 8 + 8;
    }

    public String getRecordName() {
        return "ClientTextboxPlaceholder";
    }

    
}
