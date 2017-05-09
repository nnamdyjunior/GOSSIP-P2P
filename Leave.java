/**
 * Leave
 */

import java.math.BigInteger;
import ASN1.*;

class Leave extends ASNObj {

    String peer;
    public Leave (String pr) {
        peer = pr;
    }

    public Encoder getEncoder() {
		Encoder e = new Encoder().initSequence(); 
		e.addToSequence(new Encoder(peer)
            .setASN1Type(Encoder.TAG_UTF8String));   
		return e.setExplicitASN1Tag(Encoder.CLASS_APPLICATION, 0, BigInteger.valueOf(4));
	}

    public Leave decode(Decoder dec) throws ASN1DecoderFail {
		Decoder de = dec.getContent();
		Decoder d = de.getContent();
		peer = d.getFirstObject(true).getString(Encoder.TAG_UTF8String);

		if(d.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
		return this;
	}
	public Leave instance() throws CloneNotSupportedException {return new Leave("");}
}