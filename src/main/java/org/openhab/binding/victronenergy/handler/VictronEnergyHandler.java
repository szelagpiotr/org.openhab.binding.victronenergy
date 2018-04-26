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
package org.openhab.binding.victronenergy.handler;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.KILO;
import static org.openhab.binding.victronenergy.VictronEnergyBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.victronenergy.internal.VictronEnergyConfiguration;
import org.openhab.binding.victronenergy.internal.connection.VictronEnergyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link VictronEnergyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dudzik Szelag - Initial contribution
 */
// @NonNullByDefault
public class VictronEnergyHandler extends BaseThingHandler {

    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";
    private static final String REFRESH_PARAM = "refresh";
    private static final String IDUSER_PARAM = "iduser";
    private static final String IDSITE_PARAM = "idsite";

    private final Logger logger = LoggerFactory.getLogger(VictronEnergyHandler.class);

    private final VictronEnergyConnection connection = new VictronEnergyConnection();

    private String userName = "";
    private String password = "";
    private BigDecimal iduser;
    private BigDecimal idsite;
    private BigDecimal refresh;

    private String victronData = null;
    private Gson gson = new Gson();

    static class Totals {
        double Bc;
        double Bg;
        double Gb;
        double Gc;
        double Pb;
        double Pc;
        double Pg;
        double kwh; // dlaczego nie może być inna nazwa?
    }

    class JSONVictronData {
        Totals totals;
    }

    private JSONVictronData jsonVictronData = new JSONVictronData();

    ScheduledFuture<?> refreshJob;

    @Nullable
    private VictronEnergyConfiguration config;

    public VictronEnergyHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            victronData = connection.getResponseFromQuery("", "", "");
            switch (channelUID.getId()) {
                case CHANNEL_BATTERYTOCONSUMER:
                    updateState(channelUID, getBattery2Consumer());
                    break;
                case CHANNEL_BATTERYTOGRID:
                    updateState(channelUID, getBattery2Grid());
                    break;
                case CHANNEL_GRIDTOBATTERY:
                    updateState(channelUID, getGrid2Battery());
                    break;
                case CHANNEL_GRIDTOCONSUMER:
                    updateState(channelUID, getGrid2Consumer());
                    break;
                case CHANNEL_PVTOBATTERY:
                    updateState(channelUID, getPv2Battery());
                    break;
                case CHANNEl_PVTOCONSUMER:
                    updateState(channelUID, getPv2Consumer());
                    break;
                case CHANNEL_PVTOGRID:
                    updateState(channelUID, getPv2Grid());
                    break;
                case CHANNEL_SUMINKWH:
                    updateState(channelUID, getSumInkwh());
                    break;
                default:
                    logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                    break;
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing VictronEnergy handler.");

        Configuration config = getThing().getConfiguration();

        try {
            userName = (String) config.get(USERNAME_PARAM);
            password = (String) config.get(PASSWORD_PARAM);
            refresh = (BigDecimal) config.get(REFRESH_PARAM);
            iduser = (BigDecimal) config.get(IDUSER_PARAM);
            iduser = (BigDecimal) config.get(IDSITE_PARAM);

        } catch (Exception e) {
            logger.debug("Cannot set configuration parameter.", e);
        }

        if (refresh == null) {
            // let's go for the default
            refresh = new BigDecimal(60);
        }

        if (userName == null) {
            // let's go for the default
            userName = String.valueOf("szelag@el.pcz.czest.pl");
        }

        if (password == null) {
            // let's go for the default
            password = String.valueOf("Odys2008");
        }

        if (iduser == null) {
            // let's go for the default
            iduser = BigDecimal.valueOf(14469);
        }

        if (idsite == null) {
            // let's go for the default
            idsite = BigDecimal.valueOf(10854);
        }

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
    }

    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                boolean success = false;
                victronData = connection.getResponseFromQuery("", "", "");
                if (victronData != null) {
                    success = true;
                }
                if (success) {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERYTOCONSUMER), getBattery2Consumer());
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERYTOGRID), getBattery2Grid());
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_MAX), getTemperatureMax());
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDSPEED), getWindSpeed());
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDDEG), getWindDeg());
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY), getHumidity());
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_PRESSURE), getPressure());
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_DESCRIPTION), getDescription());
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 0, refresh.intValue(), TimeUnit.MINUTES);
    }

    private State getBattery2Consumer() {
        if (victronData != null) {
            // String temp = getValue(weatherData, "main", "\"temp\":", ",");
            jsonVictronData = gson.fromJson(victronData, JSONVictronData.class);
            return new QuantityType<>(jsonVictronData.totals.Bc,
                    KILO(SmartHomeUnits.WATT).multiply(SmartHomeUnits.HOUR));
        }
        return UnDefType.UNDEF;
    }

    private State getBattery2Grid() {
        if (victronData != null) {
            // String temp = getValue(weatherData, "main", "\"temp\":", ",");
            jsonVictronData = gson.fromJson(victronData, JSONVictronData.class);
            return new QuantityType<>(jsonVictronData.totals.Bg,
                    KILO(SmartHomeUnits.WATT).multiply(SmartHomeUnits.HOUR));
        }
        return UnDefType.UNDEF;
    }

    private State getGrid2Battery() {
        if (victronData != null) {
            // String temp = getValue(weatherData, "main", "\"temp\":", ",");
            jsonVictronData = gson.fromJson(victronData, JSONVictronData.class);
            return new QuantityType<>(jsonVictronData.totals.Gb,
                    KILO(SmartHomeUnits.WATT).multiply(SmartHomeUnits.HOUR));
        }
        return UnDefType.UNDEF;
    }

    private State getGrid2Consumer() {
        if (victronData != null) {
            // String temp = getValue(weatherData, "main", "\"temp\":", ",");
            jsonVictronData = gson.fromJson(victronData, JSONVictronData.class);
            return new QuantityType<>(jsonVictronData.totals.Gc,
                    KILO(SmartHomeUnits.WATT).multiply(SmartHomeUnits.HOUR));
        }
        return UnDefType.UNDEF;
    }

    private State getPv2Battery() {
        if (victronData != null) {
            // String temp = getValue(weatherData, "main", "\"temp\":", ",");
            jsonVictronData = gson.fromJson(victronData, JSONVictronData.class);
            return new QuantityType<>(jsonVictronData.totals.Pb,
                    KILO(SmartHomeUnits.WATT).multiply(SmartHomeUnits.HOUR));
        }
        return UnDefType.UNDEF;
    }

    private State getPv2Consumer() {
        if (victronData != null) {
            // String temp = getValue(weatherData, "main", "\"temp\":", ",");
            jsonVictronData = gson.fromJson(victronData, JSONVictronData.class);
            return new QuantityType<>(jsonVictronData.totals.Pc,
                    KILO(SmartHomeUnits.WATT).multiply(SmartHomeUnits.HOUR));
        }
        return UnDefType.UNDEF;
    }

    private State getPv2Grid() {
        if (victronData != null) {
            // String temp = getValue(weatherData, "main", "\"temp\":", ",");
            jsonVictronData = gson.fromJson(victronData, JSONVictronData.class);
            return new QuantityType<>(jsonVictronData.totals.Pg,
                    KILO(SmartHomeUnits.WATT).multiply(SmartHomeUnits.HOUR));
        }
        return UnDefType.UNDEF;
    }

    private State getSumInkwh() {
        if (victronData != null) {
            // String temp = getValue(weatherData, "main", "\"temp\":", ",");
            jsonVictronData = gson.fromJson(victronData, JSONVictronData.class);
            return new QuantityType<>(jsonVictronData.totals.kwh,
                    KILO(SmartHomeUnits.WATT).multiply(SmartHomeUnits.HOUR));
        }
        return UnDefType.UNDEF;
    }

}
