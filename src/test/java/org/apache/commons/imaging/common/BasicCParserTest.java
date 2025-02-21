
/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.commons.imaging.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.imaging.ImagingException;
import org.junit.jupiter.api.Test;
import java.io.InputStream;

public class BasicCParserTest {

    //Test for unterminated comment at end of file
    @Test
    public void NotTerminatedCommentEndOfFileTest(){
        byte inputStreamString[]= "/* ".getBytes();

        InputStream is = new ByteArrayInputStream(inputStreamString);

        ImagingException exception= assertThrows(ImagingException.class,()->{ 
            BasicCParser.preprocess(is, null, null);
        });

        assertEquals("Unterminated comment at the end of file", exception.getMessage());
    }

    //Test for unterminated string at end of file
    @Test
    public void NotTerminatedStringEndOfFileTest(){
        byte inputStreamString[]= "\"hello".getBytes();

        InputStream is = new ByteArrayInputStream(inputStreamString);

        ImagingException exception= assertThrows(ImagingException.class,()->{ 
            BasicCParser.preprocess(is, null, null);
        });

        assertEquals("Unterminated string at the end of file", exception.getMessage());
    }

    //Test for unterminated string in file
    @Test
    public void NotTerminatedStringInFileTest(){
        byte inputStreamString[]= "\"\n".getBytes();

        InputStream is = new ByteArrayInputStream(inputStreamString);

        ImagingException exception= assertThrows(ImagingException.class,()->{ 
            BasicCParser.preprocess(is, null, null);
        });

        assertEquals("Unterminated string in file", exception.getMessage());
    }

    //Test for error in preprocessor directive when it is null
    @Test
    public void DefinesIsNullTest(){
        byte inputStreamString[]= "#defines".getBytes();

        InputStream is = new ByteArrayInputStream(inputStreamString);

        ImagingException exception= assertThrows(ImagingException.class,()->{ 
            BasicCParser.preprocess(is, null, null);
        });

        assertEquals("Unexpected preprocessor directive", exception.getMessage());
    }


    
}
