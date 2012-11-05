package org.trgoofi.elasticsearch;

import java.io.IOException;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Pojoes {
  static ObjectMapper mapper = new ObjectMapper();
  
  static class Pojo {
    private String id;
    @JsonIgnore private String ignore;
    @JsonProperty("username") private String name;

    public String getId() {
      return id;
    }
    
    public Pojo setId(String id) {
      this.id = id;
      return this;
    }
    
    public String getName() {
      return name;
    }
    
    public Pojo setName(String name) {
      this.name = name;
      return this;
    }

    public String getIgnore() {
      return ignore;
    }

    public Pojo setIgnore(String ignore) {
      this.ignore = ignore;
      return this;
    }
    
  }
  
  public static void main(String[] args) throws IOException {
    
    Node node = NodeBuilder.nodeBuilder().node();   // create a default node.
    Client client = node.client();
    
    client.admin().indices().prepareDelete().execute().actionGet();
    String mapId = "{\"pojo\": { \"_id\": { \"path\": \"id\" } } }";  // { "pojo": { "_id": { "path": "id" } } }"
    client.admin().indices().prepareCreate("pojoes").addMapping("pojo", mapId).execute().actionGet();
    
    Pojo pojo = new Pojo();
    pojo.setId("123").setName("trgoofi").setIgnore("this field will be ingoned");
    
    client.prepareIndex("pojoes", "pojo").setSource(mapper.writeValueAsBytes(pojo)).execute().actionGet();
    
    GetResponse response = client.prepareGet("pojoes", "pojo", "123").execute().actionGet();
    Pojo pojoBack = mapper.readValue(response.getSourceAsBytes(), Pojo.class);
    
    System.out.println(pojoBack.getName());     // trgoofi
    System.out.println(pojoBack.getIgnore());   // null
    
    client.close();
    node.close();
  }
}
