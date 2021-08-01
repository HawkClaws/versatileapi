package com.flex.versatileapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

@SpringBootTest
class ODataApiTests {
	private static Gson gson = new Gson();
	private static RestTemplate restTemplate = new RestTemplate();

	@Test
	void OData_正常() {
		String repository = "repositoryOData";
		String baseUrl = TestConfig.ApiUrl() + repository;

		String testData = "[  {    'index': 0,    'guid': '2f55025b-0360-449b-b058-f119a5613eb4',    'isActive': true,    'balance': '$3,413.33',    'picture': 'http://placehold.it/32x32',    'age': 32,    'eyeColor': 'brown',    'name': 'Candace Crawford',    'gender': 'female',    'company': 'TWIGGERY',    'email': 'candacecrawford@twiggery.com',    'phone': '+1 (988) 432-3423',    'address': '910 Nevins Street, Fostoria, Nevada, 4126',    'about': 'Est adipisicing aliquip minim anim quis commodo pariatur excepteur laboris irure eu mollit quis. Enim ullamco nostrud ea enim sit esse irure mollit et aliqua. Minim nulla irure deserunt quis eiusmod nostrud eu dolor labore pariatur qui aliquip amet elit. Occaecat aute culpa mollit nisi esse enim do nostrud qui commodo. Culpa ad incididunt id enim irure eu consequat fugiat. Excepteur sit exercitation tempor amet aliqua.\\r\\n',    'registered': '2019-02-21T10:53:23 -09:00',    'latitude': 66.834715,    'longitude': 92.098676,    'tags': [      'ex',      'proident',      'ut',      'aute',      'ea',      'anim',      'dolor'    ],    'friends': [      {        'id': 0,        'name': 'Winters Finley'      },      {        'id': 1,        'name': 'Rowe Manning'      },      {        'id': 2,        'name': 'Nelson Mckee'      }    ],    'greeting': 'Hello, Candace Crawford! You have 9 unread messages.',    'favoriteFruit': 'strawberry'  },  {    'index': 1,    'guid': '640dfb3c-ba68-49d7-9954-bb87b8242bbd',    'isActive': false,    'balance': '$1,263.48',    'picture': 'http://placehold.it/32x32',    'age': 35,    'eyeColor': 'brown',    'name': 'Alfreda Pitts',    'gender': 'female',    'company': 'PUSHCART',    'email': 'alfredapitts@pushcart.com',    'phone': '+1 (802) 464-2000',    'address': '975 Nova Court, Soham, Kansas, 3988',    'about': 'Dolore laboris mollit consectetur incididunt adipisicing magna exercitation quis. Sunt consequat tempor aliqua fugiat non exercitation nisi. Voluptate adipisicing mollit duis aute nisi nulla duis. Consectetur aliqua ex minim culpa. Cillum minim officia sint fugiat exercitation. Eiusmod excepteur occaecat laboris est deserunt velit incididunt est ad magna. Proident enim in amet elit consequat amet excepteur velit labore qui consectetur velit.\\r\\n',    'registered': '2020-01-31T02:12:01 -09:00',    'latitude': -4.230611,    'longitude': -81.312779,    'tags': [      'id',      'Lorem',      'irure',      'pariatur',      'sint',      'ullamco',      'qui'    ],    'friends': [      {        'id': 0,        'name': 'Dionne Anthony'      },      {        'id': 1,        'name': 'Ellis Higgins'      },      {        'id': 2,        'name': 'Mclaughlin Blevins'      }    ],    'greeting': 'Hello, Alfreda Pitts! You have 10 unread messages.',    'favoriteFruit': 'apple'  },  {    'index': 2,    'guid': 'e4c98da0-5c61-492f-8eba-5b8a65c2f326',    'isActive': false,    'balance': '$1,768.05',    'picture': 'http://placehold.it/32x32',    'age': 33,    'eyeColor': 'green',    'name': 'Brennan Lawrence',    'gender': 'male',    'company': 'ONTAGENE',    'email': 'brennanlawrence@ontagene.com',    'phone': '+1 (908) 530-3637',    'address': '791 Tudor Terrace, Jessie, West Virginia, 2380',    'about': 'Officia cupidatat fugiat irure anim est excepteur do deserunt do aliqua deserunt enim sunt velit. Sit minim eiusmod qui deserunt incididunt aliquip minim magna in. Velit id tempor exercitation cupidatat deserunt veniam labore amet culpa qui nostrud elit. Ea reprehenderit sint non tempor.\\r\\n',    'registered': '2016-11-25T04:25:08 -09:00',    'latitude': -3.320834,    'longitude': 43.850516,    'tags': [      'fugiat',      'mollit',      'mollit',      'nulla',      'incididunt',      'excepteur',      'consequat'    ],    'friends': [      {        'id': 0,        'name': 'Armstrong Parsons'      },      {        'id': 1,        'name': 'Mavis Alvarez'      },      {        'id': 2,        'name': 'Hawkins Boyer'      }    ],    'greeting': 'Hello, Brennan Lawrence! You have 7 unread messages.',    'favoriteFruit': 'strawberry'  },  {    'index': 3,    'guid': '42055a24-8b82-4892-a339-5caa3f974049',    'isActive': false,    'balance': '$3,346.16',    'picture': 'http://placehold.it/32x32',    'age': 38,    'eyeColor': 'green',    'name': 'Pierce Pollard',    'gender': 'male',    'company': 'ISBOL',    'email': 'piercepollard@isbol.com',    'phone': '+1 (929) 445-2165',    'address': '537 Hicks Street, Keller, New York, 1611',    'about': 'Qui nostrud dolor eu occaecat non amet velit ea qui laborum eu consectetur id anim. Nisi veniam laboris eiusmod voluptate eiusmod ex nisi velit aute sit et pariatur. Sint magna deserunt duis officia deserunt cillum fugiat aliquip. Pariatur amet labore dolor sint est cillum ad ea in nostrud. Labore consequat consectetur mollit sunt duis exercitation labore elit et nulla laborum. Id ullamco officia dolore ut cupidatat ut deserunt et amet mollit quis magna fugiat.\\r\\n',    'registered': '2017-11-04T07:48:22 -09:00',    'latitude': -26.927199,    'longitude': 100.664597,    'tags': [      'voluptate',      'non',      'officia',      'sint',      'nostrud',      'pariatur',      'nostrud'    ],    'friends': [      {        'id': 0,        'name': 'Pickett Lloyd'      },      {        'id': 1,        'name': 'Vega Lara'      },      {        'id': 2,        'name': 'Shannon Christensen'      }    ],    'greeting': 'Hello, Pierce Pollard! You have 1 unread messages.',    'favoriteFruit': 'apple'  },  {    'index': 4,    'guid': 'ef847bd3-df20-4c5d-aac6-edb6fc1e46e3',    'isActive': true,    'balance': '$1,520.72',    'picture': 'http://placehold.it/32x32',    'age': 21,    'eyeColor': 'green',    'name': 'Etta Knox',    'gender': 'female',    'company': 'EXTREMO',    'email': 'ettaknox@extremo.com',    'phone': '+1 (862) 516-2373',    'address': '694 Blake Court, Caberfae, Alabama, 827',    'about': 'Ex ex exercitation irure ea culpa nulla commodo ut deserunt eu est ex mollit. Aliqua consequat minim velit proident ullamco ut sint magna laborum est amet. Minim do non aute officia magna enim incididunt. Nisi nostrud nulla et dolor aute adipisicing sint excepteur amet.\\r\\n',    'registered': '2015-08-30T07:36:49 -09:00',    'latitude': 19.758754,    'longitude': 88.695206,    'tags': [      'tempor',      'nostrud',      'sint',      'nostrud',      'nostrud',      'esse',      'mollit'    ],    'friends': [      {        'id': 0,        'name': 'Lindsay Jarvis'      },      {        'id': 1,        'name': 'Kim West'      },      {        'id': 2,        'name': 'Consuelo Patton'      }    ],    'greeting': 'Hello, Etta Knox! You have 2 unread messages.',    'favoriteFruit': 'apple'  },  {    'index': 5,    'guid': '9b43f93d-4264-4b99-a2a3-a63de50f8dad',    'isActive': true,    'balance': '$3,822.82',    'picture': 'http://placehold.it/32x32',    'age': 23,    'eyeColor': 'green',    'name': 'Larsen Clemons',    'gender': 'male',    'company': 'GEOFORM',    'email': 'larsenclemons@geoform.com',    'phone': '+1 (836) 454-3084',    'address': '381 Seacoast Terrace, Elbert, Wisconsin, 1510',    'about': 'Quis incididunt nisi deserunt do. Amet dolor dolore eiusmod esse aute sint sit incididunt sint sit. Consequat est deserunt aliqua commodo sit excepteur. Nostrud occaecat veniam occaecat cupidatat mollit ullamco sit laboris incididunt.\\r\\n',    'registered': '2018-07-27T12:58:48 -09:00',    'latitude': 81.917945,    'longitude': 172.6543,    'tags': [      'elit',      'ut',      'cupidatat',      'dolor',      'anim',      'cupidatat',      'culpa'    ],    'friends': [      {        'id': 0,        'name': 'Santos Delacruz'      },      {        'id': 1,        'name': 'Collins Farrell'      },      {        'id': 2,        'name': 'John Barron'      }    ],    'greeting': 'Hello, Larsen Clemons! You have 2 unread messages.',    'favoriteFruit': 'banana'  }]";
		Object[] objects = gson.fromJson(testData, new Object[] {}.getClass());
		Map<String, Map<String, Object>> testJsonList = new HashMap<String, Map<String, Object>>();

		for (Object obj : objects) {
			Map<String, Object> jsonObj = gson.fromJson(gson.toJson(obj), Map.class);

			testJsonList.put(jsonObj.get("guid").toString(), jsonObj);
		}
		
		String schema = String.format("{  'authGroupId':'','jsonSchema':{  '$schema': 'http://json-schema.org/draft-04/schema#',  'type': 'object',  'properties': {    'index': {      'type': 'number'    },    'guid': {      'type': 'string'    },    'isActive': {      'type': 'boolean'    },    'balance': {      'type': 'string'    },    'picture': {      'type': 'string'    },    'age': {      'type': 'number'    },    'eyeColor': {      'type': 'string'    },    'name': {      'type': 'string'    },    'gender': {      'type': 'string'    },    'company': {      'type': 'string'    },    'email': {      'type': 'string'    },    'phone': {      'type': 'string'    },    'address': {      'type': 'string'    },    'about': {      'type': 'string'    },    'registered': {      'type': 'string'    },    'latitude': {      'type': 'number'    },    'longitude': {      'type': 'number'    },    'tags': {      'type': 'array',      'items': [        {          'type': 'string'        },        {          'type': 'string'        },        {          'type': 'string'        },        {          'type': 'string'        },        {          'type': 'string'        },        {          'type': 'string'        },        {          'type': 'string'        }      ]    },    'friends': {      'type': 'array',      'items': [        {          'type': 'object',          'properties': {            'id': {              'type': 'number'            },            'name': {              'type': 'string'            }          },          'required': [            'id',            'name'          ]        },        {          'type': 'object',          'properties': {            'id': {              'type': 'number'            },            'name': {              'type': 'string'            }          },          'required': [            'id',            'name'          ]        },        {          'type': 'object',          'properties': {            'id': {              'type': 'number'            },            'name': {              'type': 'string'            }          },          'required': [            'id',            'name'          ]        }      ]    },    'greeting': {      'type': 'string'    },    'favoriteFruit': {      'type': 'string'    }  }},'apiSecret':'','methodSettings':[],'apiUrl':'%s'}",repository);
		schema = schema.replace("'", "\"");
		

		try {
			restTemplate.delete(TestConfig.ApiSettingUrl() + repository);
		} catch (RestClientResponseException exception) {
		}
		
		TestHelper.post(TestConfig.ApiSettingUrl() , schema,TestConfig.AuthHeader());
		
		// テスト前クリーン
		restTemplate.delete(baseUrl + "/all");
		
		// 登録
		List<String> ids = new ArrayList<String>();
		for (String key : testJsonList.keySet()) {
			ids.add(TestHelper.post(baseUrl , gson.toJson(testJsonList.get(key))));
		}

		// orderby limit skip
		Object[] response1 = restTemplate.getForObject(baseUrl + "/all?$orderby=age desc&$limit=1",
				new Object[] {}.getClass());
		Object[] response2 = restTemplate.getForObject(baseUrl + "/all?$orderby=age asc&$skip=5",
				new Object[] {}.getClass());

		assertTrue(TestHelper.jsonEquals(gson.toJson(response1[0]), gson.toJson(response2[0])));

		// filter or
		response1 = restTemplate.getForObject(baseUrl
				+ "/all?$filter=name eq 'Brennan Lawrence' or email eq 'piercepollard@isbol.com'&$orderby=index asc",
				new Object[] {}.getClass());

		assertTrue(TestHelper.jsonEquals(gson.toJson(response1[0]),
				gson.toJson(testJsonList.get("e4c98da0-5c61-492f-8eba-5b8a65c2f326"))));
		assertTrue(TestHelper.jsonEquals(gson.toJson(response1[1]),
				gson.toJson(testJsonList.get("42055a24-8b82-4892-a339-5caa3f974049"))));

		// filter and
		response1 = restTemplate.getForObject(baseUrl + "/all?$filter=eyeColor eq 'green' and gender eq 'female'",
				new Object[] {}.getClass());

		assertTrue(TestHelper.jsonEquals(gson.toJson(response1[0]),
				gson.toJson(testJsonList.get("ef847bd3-df20-4c5d-aac6-edb6fc1e46e3"))));

		// filter より大きい
		response1 = restTemplate.getForObject(baseUrl + "/all?$filter=age gt 35", new Object[] {}.getClass());
		assertTrue(response1.length == 1);

		// filter 以上
		response1 = restTemplate.getForObject(baseUrl + "/all?$filter=age ge 35", new Object[] {}.getClass());
		assertTrue(response1.length == 2);
		
		// filter より小さい
		response1 = restTemplate.getForObject(baseUrl + "/all?$filter=age lt 23", new Object[] {}.getClass());
		assertTrue(response1.length == 1);
		
		// filter 以下
		response1 = restTemplate.getForObject(baseUrl + "/all?$filter=age le 23", new Object[] {}.getClass());
		assertTrue(response1.length == 2);
		
		//　not 
		response1 = restTemplate.getForObject(baseUrl + "/all?$filter=gender ne 'male'", new Object[] {}.getClass());
		assertTrue(response1.length == 3);

	}

}
