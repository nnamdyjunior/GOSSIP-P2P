/* ------------------------------------------------------------------------- */
/*   Copyright (C) 2017 
                Author:  nagu2014@my.fit.edu, sivapilot@gmail.com
                Florida Tech, Computer Science
   
       This program is free software; you can redistribute it and/or modify
       it under the terms of the GNU Affero General Public License as published by
       the Free Software Foundation; either the current version of the License, or
       (at your option) any later version.
   
      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.
  
      You should have received a copy of the GNU Affero General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.              */
/* ------------------------------------------------------------------------- */

import java.math.BigInteger;
import ASN1.*;

//SEQUENCE{val [0] IMPLICIT INTEGER, str UTF8STRING}
class Gossip extends ASNObj {
	final static byte TAG_AP0 = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION,0,(byte)0);
	
    String message;
    String timee;
    String shaval;

    public Gossip(String sha, String time, String mess){
        shaval = sha;
        timee = time;
        message = mess;
    }

	public Encoder getEncoder() {
		Encoder e = new Encoder().initSequence(); 
		e.addToSequence(new Encoder(shaval)
            .setASN1Type(Encoder.TAG_OCTET_STRING));
		e.addToSequence(new Encoder(timee)
            .setASN1Type(Encoder.TAG_GeneralizedTime));
        e.addToSequence(new Encoder(message)
            .setASN1Type(Encoder.TAG_UTF8String));    
		return e.setExplicitASN1Tag(Encoder.CLASS_APPLICATION, 0, BigInteger.valueOf(1));
	}
	public Gossip decode(Decoder dec) throws ASN1DecoderFail {
		Decoder de = dec.getContent();
		Decoder d = de.getContent();
		shaval = d.getFirstObject(true).getString(Encoder.TAG_OCTET_STRING);
        //System.out.println(shaval);
		timee = d.getFirstObject(true).getGeneralizedTime(Encoder.TAG_GeneralizedTime);
        message = d.getFirstObject(true).getString(Encoder.TAG_UTF8String);

		if(d.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
		return this;
	}
	public Gossip instance() throws CloneNotSupportedException {return new Gossip("","","");}
}