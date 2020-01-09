/*
 * Copyright (c) 2019. Alexis Ringot, Florian Taurer, Matthias Zeppelzauer.
 *
 * This file is part of SoniTalk Android SDK.
 *
 * SoniTalk Android SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SoniTalk Android SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SoniTalk Android SDK.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.ac.fhstp.sonitalk.utils;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import at.ac.fhstp.sonitalk.SoniTalkConfig;
import at.ac.fhstp.sonitalk.SoniTalkHeader;
import at.ac.fhstp.sonitalk.SoniTalkMessage;
import at.ac.fhstp.sonitalk.SoniTalkMultiMessage;

/**
 * The main part of EncoderUtils is to change the message bytes to bit.
 * Beside that it checks for characters with more bytes and offers a function
 * to check if the number of bytes exceed a specific value.
 */
public class EncoderUtils {

    /**
     * Casts a byte array to bit and checks for characters with more than one byte.
     * @param textToSend text to cast and check
     * @return bit sequence as string
     */
    public static String changeToBitString(byte[] textToSend) {
        String bitOfText = "";

        for (byte b : textToSend) {
            if(Integer.toBinaryString(b).length()>8){
                bitOfText = bitOfText + checkOnFullByteString(Integer.toBinaryString(b).substring(Integer.toBinaryString(b).length()-8));
            }else {
                bitOfText = bitOfText + checkOnFullByteString(Integer.toBinaryString(b));
            }
        }

        CRC.countOccurrences(bitOfText,'1');

        byte[] outputByte = new byte[bitOfText.length()/8];
        int counter = 0;
        for(int i = 0; i <= bitOfText.length() - 8; i+=8)
        {
            if((i+32) <= bitOfText.length() && bitOfText.substring(i, i+3).equals("1111")) {
                int k = Integer.parseInt(bitOfText.substring(i, i + 32), 2);
                outputByte[counter++] = (byte)k;
                i+=24;
            }else if((i+24) <= bitOfText.length() && bitOfText.substring(i, i+2).equals("111")) {
                int k = Integer.parseInt(bitOfText.substring(i, i + 24), 2);
                outputByte[counter++] = (byte)k;
                i+=16;
            }else if((i+16) <= bitOfText.length() && bitOfText.substring(i, i+1).equals("11")) {
                int k = Integer.parseInt(bitOfText.substring(i, i + 16), 2);
                outputByte[counter++] = (byte)k;
                i+=8;
            }else {
                int k = Integer.parseInt(bitOfText.substring(i, i + 8), 2);
                outputByte[counter++] = (byte)k;
            }
        }

        return bitOfText;
    }

    /**
     * Uses byte array length check and returns the casted bit sequence or Exception.
     * @param textToSend text to check
     * @param config SoniTalkConfig to get number of message blocks
     * @return casted bit sequence or throws IllegalArgument if size exceeded
     */
    public String getStringOfEncodedBits(byte[] textToSend, SoniTalkConfig config){
        String bitOfText = changeToBitString(textToSend);

        if(isAllowedByteArraySize(textToSend, config)) {
            return bitOfText;
        }else{
            throw new IllegalArgumentException("Entered Message is too long");
        }
    }

    public String getStringOfEncodedBits(byte[] textToSend, byte messageId, byte packetId, byte numberOfPackets, SoniTalkConfig config){
        byte[] fullMessage = null;
        fullMessage = ArrayUtils.addAll(fullMessage,messageId);
        fullMessage = ArrayUtils.addAll(fullMessage,packetId);
        fullMessage = ArrayUtils.addAll(fullMessage,numberOfPackets);
        fullMessage = ArrayUtils.addAll(fullMessage,textToSend);
        String bitOfText = changeToBitString(fullMessage);

        if(isAllowedByteArraySize(textToSend, config)) {
            return bitOfText;
        }else{
            throw new IllegalArgumentException("Entered Message is too long");
        }
    }

    /**
     * Checks if the byte array does not exceed the number of allowed bytes.
     * @param textToSend byte array to check
     * @param config SoniTalkConfig to get number of message blocks
     * @return boolean depending on the number of bytes
     */
    public static boolean isAllowedByteArraySize(byte[] textToSend, SoniTalkConfig config){
        int maxChars =  config.getnMessageBlocks()*(config.getnFrequencies()/8)-2;
        Log.d("EncoderUtils", "isAllowedByteArraySize "+maxChars);
        if(changeToBitString(textToSend).length()<=(maxChars*8)) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * Checks if every character uses eight bit. If not a zero is added at the beginning of the character.
     * @param bitOfText text to check
     * @return checked character string
     */
    private static String checkOnFullByteString(String bitOfText){
        String helpString;
        if((Float.valueOf(bitOfText.length()) % 8) != 0){
            helpString = "0" + bitOfText;
            helpString = checkOnFullByteString(helpString);
        }else{
            helpString = bitOfText;
        }
        return helpString;
    }

    public static byte intToByteArray(int a)
    {
        byte ret = (byte) a;
        return ret;
    }


    public static int calculateNumberOfPackets(SoniTalkMultiMessage message, SoniTalkConfig config){
        int numOfBytes = config.getnMessageBlocks()*(config.getnFrequencies()/8)-2;
        int fixedHeaderSize = 3; //4 Byte reserved = 1 Byte messageId, 1 Byte packetId, 1 Byte numberOfPackets
        final byte[] bytes = message.getMessage();
        byte[] headerBytesPlaceholder = new byte[fixedHeaderSize];
        byte[] checkSizeByte = ArrayUtils.addAll(bytes, headerBytesPlaceholder);
        if(EncoderUtils.isAllowedByteArraySize(checkSizeByte, config)){
            return 1;
        } else{
            if(bytes.length%(numOfBytes-fixedHeaderSize)==0){
                return bytes.length/(numOfBytes-fixedHeaderSize);
            }else{
                return bytes.length/(numOfBytes-fixedHeaderSize)+1;
            }
        }
    }

    public static SoniTalkMessage[] splitMultiMessageIntoSoniTalkMessages(SoniTalkMultiMessage message, int numberOfPackets, SoniTalkConfig config){
        SoniTalkMessage[] soniTalkMessages = new SoniTalkMessage[numberOfPackets];
        int numOfBytes = config.getnMessageBlocks()*(config.getnFrequencies()/8)-2;
        int fixedHeaderSize = 3; //4 Byte reserved = 1 Byte messageId, 1 Byte packetId, 1 Byte numberOfPackets
        int messageId = (int) (255*Math.abs(Math.random()));
        int packetId = 1;
        SoniTalkHeader soniTalkHeader;// = new SoniTalkHeader(EncoderUtils.intToByteArray(messageId), EncoderUtils.intToByteArray(packetId), EncoderUtils.intToByteArray(numberOfPackets));
        for(int i = 0; i<numberOfPackets ;i++){
            byte[] messageBodyPart;
            if((i+1)!=numberOfPackets){
                int messageLength = numOfBytes-fixedHeaderSize;
                messageBodyPart = new byte[messageLength];
                System.arraycopy(message.getMessage(), messageLength*i, messageBodyPart,0, messageLength);
            }else{
                int restMessageLength = message.getMessage().length-((numOfBytes-fixedHeaderSize)*(numberOfPackets-1));
                messageBodyPart = new byte[restMessageLength];
                System.arraycopy(message.getMessage(), (numOfBytes-fixedHeaderSize)*(numberOfPackets-1), messageBodyPart,0, restMessageLength);
            }
            soniTalkHeader = new SoniTalkHeader(EncoderUtils.intToByteArray(messageId), EncoderUtils.intToByteArray(packetId), EncoderUtils.intToByteArray(numberOfPackets));
            soniTalkMessages[i] = new SoniTalkMessage(messageBodyPart, soniTalkHeader);
            packetId++;
        }

        return soniTalkMessages;
    }

}
