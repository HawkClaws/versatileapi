JsonをPost(API定義を設定)するだけで、CRUDのRESTWebapiを作れるシステムです
https://qiita.com/HawkClaws/items/6f5a6938318fb7d7983d

下記3つの機能があります
- Jsonスキーマバリデーション
- メソッド許可（GET,POST,PUT,DELETE）の指定
- パスワード認証


|  item  |  Technology  |
| ---- | ---- |
|  lang  |  Java  |
|  Framework  |  SpringBoot  |
|  DB  |  MongoDB  |

SampleDifin（API定義）

```number-battle.json
{
	"apiSecret": "",
	"apiUrl":"number_battle",
	"jsonSchema": {
        "additionalProperties":false,
		"type": "object",
		"properties": {
			"name": {
                "description": "名前",
				"type": "string",
                "maxLength": 30
			},
            "secondNumber": {
                "description": "２番目に大きいと予想した数字",
				"type": "number"
			}
		},
		"required": [
			"name",
			"secondNumber"
		]
	},
	"methodSettings": [
		{
			"httpMethod": "GET",
			"behavior": "Allow"
		},
        {
			"httpMethod": "POST",
			"behavior": "Allow"
		},
        {
			"httpMethod": "PUT",
			"behavior": "NotImplemented"
		},
        {
			"httpMethod": "DELETE",
			"behavior": "NotImplemented"
		}
	]
}
```

TODO
- JsonSchemaでFKバリデーション実装
- 認証周り実装
![image](https://user-images.githubusercontent.com/62013138/126884266-52b789fc-516b-4594-8a5d-983edd845e74.png)

