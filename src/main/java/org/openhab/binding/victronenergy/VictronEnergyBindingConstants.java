/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.victronenergy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VictronEnergyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dudzik Szelag - Initial contribution
 */
@NonNullByDefault
public class VictronEnergyBindingConstants {

    private static final String BINDING_ID = "victronenergy";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "victronenergy");

    // List of all Channel ids

    public static final String CHANNEL_1 = "channel1";
    public static final String CHANNEL_PVTOGRID = "pvtogrid";
    public static final String CHANNEl_PVTOCONSUMER = "pvtoconsumer";
    public static final String CHANNEL_PVTOBATTERY = "pvtobattery";

    public static final String CHANNEL_GRIDTOCONSUMER = "gridtoconsumer";
    public static final String CHANNEL_GRIDTOBATTERY = "gridtobattery";

    public static final String CHANNEL_BATTERYTOCONSUMER = "batterytoconsumer";
    public static final String CHANNEL_BATTERYTOGRID = "batterytogrid";

    public static final String CHANNEL_SUMINKWH = "suminkwh";

}
