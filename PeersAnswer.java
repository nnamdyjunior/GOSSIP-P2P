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

import java.util.ArrayList;
import java.math.BigInteger;
import ASN1.*;

/**
 * PeersAnswer
 */
class PeersAnswer extends ASNObj{

    ArrayList<PeerInfo> peers;
    public PeersAnswer (ArrayList<PeerInfo> prs) {
        peers = prs;
    }

    public Encoder getEncoder() {
		Encoder e = new Encoder().initSequence();
		for(PeerInfo pi : peers){
            Peer pr = new Peer(pi.name, pi.port, pi.ip_address);
            byte[] pr_bytes = pr.encode();
            e.addToSequence(pr_bytes);
        }

		return e.setExplicitASN1Tag(Encoder.CLASS_APPLICATION, 0, BigInteger.valueOf(2));
	}
	public PeersAnswer decode(Decoder dec) throws ASN1DecoderFail {
        Decoder de = dec.getContent();
		Decoder d = de.getContent();

        peers = new ArrayList<>();
        //peers = d.GETFIRSTOBJECT(TRUE).getSequenceOfAL(Encoder.CLASS_APPLICATION, new PeerInfo("", 0, ""));
        while(d.getTypeByte() != 0){
            byte[] byt = d.getFirstObject(true).getBytes();
            Decoder deco = new Decoder(byt);
            //deco = deco.getContent();
            String one = deco.getFirstObject(true).getString();
            int two = deco.getFirstObject(true).getInteger().intValue();
            String three = deco.getFirstObject(true).getString();
            PeerInfo prInfo = new PeerInfo(one, two, three);
            peers.add(prInfo);
        }
		//if(d.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
		return this;
	}
	public PeersAnswer instance() throws CloneNotSupportedException {return new PeersAnswer(peers);}
}