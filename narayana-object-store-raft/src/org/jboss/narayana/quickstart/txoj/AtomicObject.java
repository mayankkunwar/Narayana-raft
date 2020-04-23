/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.quickstart.txoj;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

import java.io.IOException;
import java.io.Serializable;

public class AtomicObject extends LockManager implements Serializable {

    private static final long serialId = 1453443423232L;
    private int state;

    public AtomicObject() throws Exception {
        super();

        state = 0;

        if (setlock(new Lock(LockMode.WRITE), defaultRetry, defaultSleepTime) == LockResult.GRANTED) {
            System.out.println("Created persistent object " + get_uid());
        } else {
            throw new Exception("setlock error.");
        }
    }

    public void incr(int value) throws Exception {

        if (setlock(new Lock(LockMode.WRITE), defaultRetry, defaultSleepTime) == LockResult.GRANTED) {
            state += value;

            return;
        } else {
            throw new Exception("Write lock error.");
        }
    }

    public void set(int value) throws Exception {
        if (setlock(new Lock(LockMode.WRITE), defaultRetry, defaultSleepTime) == LockResult.GRANTED) {
            state = value;
            return;
        } else {
            throw new Exception("Write lock error.");
        }
    }

    public int get() throws Exception {
        if (setlock(new Lock(LockMode.READ), defaultRetry, defaultSleepTime) == LockResult.GRANTED) {
            return state;
        } else {
            throw new Exception("Read lock error.");
        }
    }

    public boolean save_state(OutputObjectState os, int ot) {
        boolean result = super.save_state(os, ot);

        if (!result)
            return false;

        try {
            os.packInt(state);
        } catch (IOException e) {
            result = false;
        }

        return result;
    }

    public boolean restore_state(InputObjectState os, int ot) {
        boolean result = super.restore_state(os, ot);

        if (!result)
            return false;

        try {
            state = os.unpackInt();
        } catch (IOException e) {
            result = false;
        }

        return result;
    }

    public String type() {
        return "/StateManager/LockManager/AtomicObject";
    }
}