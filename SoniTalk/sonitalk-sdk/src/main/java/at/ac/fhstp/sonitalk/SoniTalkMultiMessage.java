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

package at.ac.fhstp.sonitalk;

import java.util.Arrays;

/**
 * Wrapper class for messages (received or to be sent).
 * crcIsCorrect, decodingTimeNanosecond are mostly used for debugging purpose and should not be
 * accessed during normal usage of the library.
 * rawAudio is optional for received messages (also a debugging support)
 */
public class SoniTalkMultiMessage {
    /**
     * Actual message, received or to be sent. DecoderUtils contains functions to transform it
     * to/from UTF8 String if that is the kind of data you use.
     */
    private byte[] message;
    /**
     * crcIsCorrect is true by default (if not provided, we consider the message to be useful)
     */
    private boolean crcIsCorrect;

    public SoniTalkMultiMessage(byte[] message) {
        this.message = message;
        crcIsCorrect = true;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public boolean isCrcCorrect() {
        return crcIsCorrect;
    }

    public void setCrcIsCorrect(boolean crcIsCorrect) {
        this.crcIsCorrect = crcIsCorrect;
    }

}
