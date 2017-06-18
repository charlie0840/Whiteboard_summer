/******************************************************************************
 * Copyright 2013, Qualcomm Innovation Center, Inc.
 *
 *    All rights reserved.
 *    This file is licensed under the 3-clause BSD license in the NOTICE.txt
 *    file for this project. A copy of the 3-clause BSD license is found at:
 *
 *        http://opensource.org/licenses/BSD-3-Clause.
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the license is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the license for the specific language governing permissions and
 *    limitations under the license.
 ******************************************************************************/

package com.example.charlie0840.whiteboard1;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;


@BusInterface(name = "com.example.charlie0840.whiteboard1.SimpleInterface")
public interface SimpleInterface {

    @BusSignal
    public void Ping(byte[] pathArray, byte[] picArray, Integer color, String brush, String Str, String xAxis, String yAxis,
                     Integer split, String groupname, Integer asked, boolean reset, byte[] oldPicArra, boolean undo, boolean redo) throws BusException;

}
