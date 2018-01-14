/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fluxtion.visualiser.extensions.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * <p>EventLogLoader class.</p>
 *
 * @author Greg Higgins (greg.higgins@V12technology.com)
 * @version $Id: $Id
 */
public class EventLogLoader {

    private List<EventLog> eventList = new ArrayList<>();

    /**
     * <p>Constructor for EventLogLoader.</p>
     */
    public EventLogLoader() {
        loadSampleLog();
    }

    /**
     * <p>Getter for the field <code>eventList</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<EventLog> getEventList() {
        return eventList;
    }

    /**
     * Load a file Display getEventType summary
     *
     * select getEventType and display
     *
     * move through record with up/down arrow keys Display log message on
     * selection
     *
     * required functions from display panel: filterNodesById(String[] ids)
     * highlightNode(String id) load from scratch
     *
     *
     *
     */
    private void loadSampleLog() {
        eventList.clear();
        new Yaml()
                .loadAll(eventLogSingle + "\n---\n" + eventLogSingle)
                .forEach(m -> {
                    Map e = (Map) ((Map) m).get("eventLogRecord");
                    eventList.add(new EventLog(e));
                });
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        new EventLogLoader();
    }

    private final String eventLogSingle = "eventLogRecord: {\n"
            + "  logTime: 1502727932750,\n"
            + "  groupingId: null,\n"
            + "  event: NewOrderRequest,\n"
            + "  nodeLogs: [\n"
            + "    orderFilter_global: { newOrderId: 1009, ccyPair: EURUSD, orderSize: 4700000, traderId: hedger_1},\n"
            + "    orderFilter_EURUSD: { newOrderId: 1009, ccyPair: EURUSD, orderSize: 4700000, traderId: hedger_1},\n"
            + "    orderFilter_hedger_1_USDJPY: { ignored: true},\n"
            + "    orderFilter_EURJPY: { ignored: true},\n"
            + "    orderFilter_hedger_1_EURJPY: { ignored: true},\n"
            + "    orderFilter_USDCHF: { ignored: true},\n"
            + "    orderFilter_hedger_1_USDCHF: { ignored: true},\n"
            + "    cache_global: { newOrder: 1009, pendingOrders: 1, liveOrders: 2},\n"
            + "    cache_EURUSD: { newOrder: 1009, pendingOrders: 1, liveOrders: 2},\n"
            + "    orderFilter_minOrderSize_4000000: { filtered: true, orderSize: 4700000, minimumSize: 4000000, parentFilterValid: true},\n"
            + "    killSwitchRule_global: { failedValidation: false},\n"
            + "    boundedOrderRule_global: { failedValidation: false, orderSize: 4700000, minSize: 10, maxSize: 10000000},\n"
            + "    duringOpeningHoursRule_global: { failedValidation: false, marketOpen: true, openHour: 2, closeHour: 22, currentHour: 20},\n"
            + "    maxLiveOrdersRule_EURUSD: { failedValidation: false, liveOrders: 2, maxLiveOrders: 4},\n"
            + "    boundedOrderRule_EURUSD: { failedValidation: false, orderSize: 4700000, minSize: 1000000, maxSize: 5000000},\n"
            + "    killSwitchRule_EURUSD: { failedValidation: false},\n"
            + "    cache_minOrderSize_4000000: { newOrder: 1009, pendingOrders: 1, liveOrders: 2},\n"
            + "    validator_global: { validated: true},\n"
            + "    validator_EURUSD: { validated: true},\n"
            + "    maxLiveOrdersRule_minOrderSize_4000000: { failedValidation: true, errorCode: 2, liveOrders: 2, maxLiveOrders: 2},\n"
            + "    validator_minOrderSize_4000000: { validated: false},\n"
            + "    orderPublisher: { rejectOrderRequest: true, orderId: 1009, ccyPair: EURUSD, traderId: hedger_1},\n"
            + "    cache_global: { failedValidation: 1009, pendingOrders: 0, liveOrders: 2},\n"
            + "    cache_EURUSD: { failedValidation: 1009, pendingOrders: 0, liveOrders: 2},\n"
            + "    cache_hedger_1_USDJPY: { ignored: true},\n"
            + "    cache_EURJPY: { ignored: true},\n"
            + "    cache_hedger_1_EURJPY: { ignored: true},\n"
            + "    cache_USDCHF: { ignored: true},\n"
            + "    cache_hedger_1_USDCHF: { ignored: true},\n"
            + "    cache_minOrderSize_4000000: { failedValidation: 1009, pendingOrders: 0, liveOrders: 2}\n"
            + "  ]\n"
            + "}";

}
