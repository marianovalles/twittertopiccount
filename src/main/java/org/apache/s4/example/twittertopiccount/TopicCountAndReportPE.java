/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.s4.example.twittertopiccount;

import org.apache.s4.dispatcher.EventDispatcher;
import org.apache.s4.processor.AbstractPE;

public class TopicCountAndReportPE extends AbstractPE {
    private String id;
    private transient EventDispatcher dispatcher;
    private String outputStreamName;
    private int threshold;
    private int count;

    public void setId(String id) {
        this.id = id;
    }

    public EventDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public String getOutputStreamName() {
        return outputStreamName;
    }

    public void setOutputStreamName(String outputStreamName) {
        this.outputStreamName = outputStreamName;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void processEvent(TopicSeen topicSeen) {
        count += topicSeen.getCount();
        TopicSeen topicSeenToSend = new TopicSeen((String) this.getKeyValue().get(0),
                count,topicSeen.getTweet(), topicSeen.getUserid() );
        topicSeenToSend.setReportKey("1");
        dispatcher.dispatchEvent(outputStreamName, topicSeenToSend);
    }

    @Override
    public void output() {
//        //This is now 1 in the xml
//    	if (count < threshold) {
//            return;
//        }
        
    }

    @Override
    public String getId() {
        return this.id;
    }

}
