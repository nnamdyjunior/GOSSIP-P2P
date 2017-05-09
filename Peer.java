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
import java.util.Arrays;
import ASN1.*;

/**
 * Peer
 */
class Peer extends ASNObj{

    String name, ip_address;
    int port;
    public Peer (String nom, int prt, String ipadd){
        name = nom;
        port = prt;
        ip_address = ipadd;
    }

    public Encoder getEncoder() {
		Encoder e = new Encoder().initSequence();
		e.addToSequence(
			new Encoder(name));
		e.addToSequence(new Encoder(port)
            .setASN1Type(Encoder.TAG_INTEGER));
        e.addToSequence(new Encoder(ip_address)
            .setASN1Type(Encoder.TAG_PrintableString));

		return e.setASN1Type(Encoder.CLASS_APPLICATION, 0, BigInteger.valueOf(2));
	}
	public Peer decode(Decoder dec) throws ASN1DecoderFail {
		Decoder d = dec.getContent();
		name = d.getFirstObject(true).getString();
        //System.out.println(name);
		port = d.getFirstObject(true).getInteger(Encoder.TAG_INTEGER).intValue();
        ip_address = d.getFirstObject(true).getString(Encoder.TAG_PrintableString);

		if(d.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
		return this;
	}
	public Peer instance() throws CloneNotSupportedException {return new Peer("",0,"");}
}