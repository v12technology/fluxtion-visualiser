/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fluxtion.visualiser.yaml;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.Ignore;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Greg Higgins (greg.higgins@V12technology.com)
 */
public class LoadEventLogTest {

    public static class EventLog {

        private Map map;
        private List<AuditRecord> auditList;
        private List<String> nodeNameList;

        public void setMap(Map map) {
            this.map = map;
            auditList = null;
            nodeNameList = null;
        }

        public long logTime() {
            return (long) map.get("logTime");
        }

        public String groupingId() {
            return (String) map.get("groupingId");
        }

        public String event() {
            return (String) map.get("event");
        }

        public List< Map> nodeLogs() {
            return (List< Map>) map.get("nodeLogs");
        }

        public List< AuditRecord> auditLogs() {
            if (auditList == null) {
                LongAdder accumulator = new LongAdder();
                auditList = ((List< Map>) map.get("nodeLogs")).stream().map(m -> {
                    Map.Entry entry = (Map.Entry) m.entrySet().iterator().next();
                    final AuditRecord auditRecord = new AuditRecord(entry, accumulator.intValue());
                    accumulator.increment();
                    return auditRecord;
                }).collect(Collectors.toList());
            }
            return auditList;
        }

        public List<String> nodeNames() {
            if (nodeNameList == null) {
                nodeNameList = (List<String>) nodeLogs().stream().flatMap(f -> f.keySet().stream()).map(k -> (String) k).distinct().collect(Collectors.toList());
            }
            return nodeNameList;
        }

        public int logCount() {
            return nodeLogs().size();
        }

        @Override
        public String toString() {
            return "EventLog{"
                    + "logTime=" + logTime()
                    + ", groupingId=" + groupingId()
                    + ", event=" + event()
                    + ", logCount=" + logCount()
                    + ", auditLogs=" + auditLogs()
                    + ", nodeLogs=" + nodeLogs()
                    + ", nodeNames=" + nodeNames()
                    + '}';
        }

        public static class AuditRecord {

            private final String nodeId;
            private final Map propertyMap;
            private final int sequenceNumber;

            public AuditRecord(Map.Entry entry, int sequenceNumber) {
                this.nodeId = (String) entry.getKey();
                this.propertyMap = (Map) entry.getValue();
                this.sequenceNumber = sequenceNumber;
            }

            public String nodeId() {
                return nodeId;
            }

            public Map propertyMap() {
                return propertyMap;
            }

            public int sequenceNumber() {
                return sequenceNumber;
            }

            @Override
            public String toString() {
                return "AuditRecord{"
                        + "nodeId=" + nodeId
                        + ", sequenceNumber=" + sequenceNumber
                        + ", propertyMap=" + propertyMap
                        + '}';
            }

        }

    }

    @Test
    public void loadEventLog() {
        Yaml yaml = new Yaml();
        Iterable<Object> input = yaml.loadAll(eventLogSingle + "\n---\n" + eventLogSingle);
//        Iterable<Object> input = yaml.loadAll(message);
        EventLog log = new EventLog();
        input.forEach(e -> {
            log.setMap((Map) ((Map) e).get("eventLogRecord"));
            System.out.println(log);
        });
//        System.out.println("loaded:" + input);
    }

    @Test
    @Ignore
    public void loadEventLogAsType() {
//        Representer representer = new Representer();
//        Tag t = new Tag("eventLogRecord");
//        representer.addClassTag(EventLog.class,t);
//        Yaml yaml = new Yaml(representer);
//        System.out.println(yaml.dumpAs(new EventLog(), Tag.MAP, null));
////        yaml.addImplicitResolver(Tag.MAP, Pattern.compile("xxx"), eventLogSingle);
//        EventLog input = yaml.loadAs(eventLogSingle, EventLog.class);
//////        input.forEach(System.out::println);
////        System.out.println("loaded:" + input);
    }

    private final String message = "record: { id: 12}\n---\nrecord: { id: 23}\n";

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
