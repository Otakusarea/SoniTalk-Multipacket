package at.ac.fhstp.sonitalk;

import at.ac.fhstp.sonitalk.utils.EncoderUtils;

public class SoniTalkHeader {

    private byte messageId;
    private byte packetId;
    private byte numberOfPackets;
    //private byte[] packetNumber;

    public SoniTalkHeader(byte messageId, byte packetId, byte numberOfPackets/*, byte[] packetNumber*/){
        //this.numberOfPackets = EncoderUtils.intToByteArray(numberOfPackets);
        //this.packetNumber = EncoderUtils.intToByteArray(packetNumber);
        this.messageId = messageId;
        this.packetId = packetId;
        this.numberOfPackets = numberOfPackets;
        //this.packetNumber = packetNumber;
    }

    public byte getMessageId() {
        return messageId;
    }

    public byte getPacketId() {
        return packetId;
    }

    public byte getNumberOfPackets() {
        return numberOfPackets;
    }

    /*public byte[] getPacketNumber() {
        return packetNumber;
    }*/
}
