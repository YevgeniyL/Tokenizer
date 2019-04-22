## Payment Processor (payment process simulation, Tokenizer)

**Components:**
•	API Layer (HTTP)
•	Messaging Bus (KAFKA)
•	Persistence Layer (REDIS)

**Acceptance criteria:**

###Data Source (API Layer, module #1):
-	Client sends HTTP request to payment processor tokenizer (application/json':{"cardNumber": "400734534535000000027",....)
-	Application produces JSON to KAFKA INPUT topic
-	Application returns HTTP response:
{
	"success": true,
	"transactionId":  [UNIQUE GENERATED ID]
}

### Data Flow (Tokenizer, module #2):
-	Application consumes data from the API Layer Source (KAFKA INPUT topic)
-	Application encrypts sensitive information by using of symmetric-key algorithm:

Func (cardNumber, expirationDate, cardNumber) => [TOKEN, KEY]

### Data Sink (Tokenizer, module #2):
-	Application produces JSON data to KAFKA OUTPUT topic:
{
	"transactionId":  …,
	"token":  …
}

-	Application puts decryption key to REDIS storage


### Data Proof (Consumer, module #3):
-	Application consumes data from KAFKA OUTPUT topic
-	Application finds decryption key in REDIS storage
-	Application decrypts token:

Func (TOKEN, KEY) => [cardNumber, expirationDate, cardNumber]
-	Application prints JSON to console:
{
	"transactionId": {
		" cardNumber ":  …,
		" expirationDate": …,
		" cardNumber": …
	}
}


**Creating kafka-topics:**
- path/to/kafka-topics.sh --create --topic data-input \ --replication-factor 1 \ --partitions N
- path/to/kafka-topics.sh --create --topic data-output \ --replication-factor 1 \ --partitions N

**Maven packaging:**
-	mvn package -pl source/
-	mvn package -pl flow/
-	mvn package -pl proof/

**Running:**
-	java -Dconfig.file=path/to/config -jar proof/target/proof-1.0.0.jar
-	java -Dconfig.file=path/to/config -jar flow/target/flow-1.0.0.jar
-	java -Dconfig.file=path/to/config -jar source/target/source-1.0.0.jar

**Example POST-request with 'Content-Type: application/json':**
{
		"cardNumber": "400734534535000000027",
		"expirationDate": "02/20",
		"cvcNumber": "123"
}

