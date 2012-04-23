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

import org.apache.s4.ft.DefaultFileSystemStateStorage;
import org.apache.s4.persist.Persister;
import org.apache.s4.processor.AbstractPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

public class TopNTopicPE extends AbstractPE {
	private static org.apache.log4j.Logger logger = Logger.getLogger(TopNTopicPE.class);
	private String id;
    private transient Persister persister;
    private int entryCount = 10;
    private Map<String, Integer> topicMap = new ConcurrentHashMap<String, Integer>();
    private Map<String, String> tweetsMap = new ConcurrentHashMap<String, String>();
    private int persistTime;
    private String persistKey = "myapp:topNTopics";

    public void setId(String id) {
        this.id = id;
    }

    public Persister getPersister() {
        return persister;
    }

    public void setPersister(Persister persister) {
        this.persister = persister;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }

    public int getPersistTime() {
        return persistTime;
    }

    public void setPersistTime(int persistTime) {
        this.persistTime = persistTime;
    }

    public String getPersistKey() {
        return persistKey;
    }

    public void setPersistKey(String persistKey) {
        this.persistKey = persistKey;
    }
    
    public void processEvent(TopicSeen topicSeen) {
        topicMap.put(topicSeen.getTopic(), topicSeen.getCount());
        tweetsMap.put(topicSeen.getTopic(), topicSeen.getTweet());
    }

    public ArrayList<TopNEntry> getTopTopics() {
        if (entryCount < 1) 
            return null;

        ArrayList<TopNEntry> sortedList = new ArrayList<TopNEntry>();

        for (String key : topicMap.keySet()) {
            sortedList.add(new TopNEntry(key, topicMap.get(key), tweetsMap.get(key)));
        }

        Collections.sort(sortedList);

        // truncate: Yuck!!
        // unfortunately, Kryo cannot deserialize RandomAccessSubList
        // if we use ArrayList.subList(...)
        while (sortedList.size() > entryCount)
            sortedList.remove(sortedList.size() - 1);

        return sortedList;
    }

    @Override
    public void output() {
        List<TopNEntry> sortedList = new ArrayList<TopNEntry>();

        for (String key : topicMap.keySet()) {	
        	//logger.debug(tweetsMap.get(key));
            sortedList.add(new TopNEntry(key, topicMap.get(key), tweetsMap.get(key)));
        }

        Collections.sort(sortedList);

        try {
            JSONObject message = new JSONObject();
            JSONArray jsonTopN = new JSONArray();

            for (int i = 0; i < entryCount; i++) {
                if (i == sortedList.size()) {
                    break;
                }
                TopNEntry tne = sortedList.get(i);
                JSONObject jsonEntry = new JSONObject();
                jsonEntry.put("topic", tne.getTopic());
                jsonEntry.put("count", tne.getCount());
                jsonEntry.put("lastTweet", tne.getTweet());
                jsonTopN.put(jsonEntry);
            }
            message.put("topN", jsonTopN);
            persister.set(persistKey, message.toString()+"\n", persistTime);
        } catch (Exception e) {
            Logger.getLogger("s4").error(e);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    public static class TopNEntry implements Comparable<TopNEntry> {
        
		public TopNEntry(String topic, int count, String tweet) {
            this.topic = topic;
            this.count = count;
            this.tweet = tweet;
        }

        public TopNEntry() {}
       
		String tweet=null;
        String topic = null;
        int count = 0;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
        public String getTweet() {
			return tweet;
		}

		public void setTweet(String tweet) {
			this.tweet = tweet;
		}

        public int compareTo(TopNEntry topNEntry) {
            if (topNEntry.getCount() < this.count) {
                return -1;
            } else if (topNEntry.getCount() > this.count) {
                return 1;
            }
            return 0;
        }

        public String toString() {
            return "topic:" + topic + " count:" + count + "tweet:" + tweet;
        }
    }
}
