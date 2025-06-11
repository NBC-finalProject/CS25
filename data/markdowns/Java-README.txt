{
    blabla....
  }
  ```

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-2-1-java)

</br>

## Access Modifier

변수 또는 메소드의 접근 범위를 설정해주기 위해서 사용하는 Java 의 예약어를 의미하며 총 네 가지 종류가 존재한다.

* public  
  어떤 클래스에서라도 접근이 가능하다.

* protected  
  클래스가 정의되어 있는 해당 패키지 내 그리고 해당 클래스를 상속받은 외부 패키지의 클래스에서 접근이 가능하다.

* (default)  
  클래스가 정의되어 있는 해당 패키지 내에서만 접근이 가능하도록 접근 범위를 제한한다.

* private  
  정의된 해당 클래스에서만 접근이 가능하도록 접근 범위를 제한한다.

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-2-1-java)

</br>

## Wrapper class

기본 자료형(Primitive data type)에 대한 클래스 표현을 Wrapper class 라고 한다. `Integer`, `Float`, `Boolean` 등이 Wrapper class 의 예이다. int 를 Integer 라는 객체로 감싸서 저장해야 하는 이유가 있을까? 일단 컬렉션에서 제네릭을 사용하기 위해서는 Wrapper class 를 사용해줘야 한다. 또한 `null` 값을 반환해야만 하는 경우에는 return type 을 Wrapper class 로 지정하여 `null`을 반환하도록 할 수 있다. 하지만 이러한 상황을 제외하고 일반적인 상황에서 Wrapper class 를 사용해야 하는 이유는 객체지향적인 프로그래밍을 위한 프로그래밍이 아니고서야 없다. 일단 해당 값을 비교할 때, Primitive data type 인 경우에는 `==`로 바로 비교해줄 수 있다. 하지만 Wrapper class 인 경우에는 `.intValue()` 메소드를 통해 해당 Wrapper class 의 값을 가져와 비교해줘야 한다.

### AutoBoxing

JDK 1.5 부터는 `AutoBoxing`과 `AutoUnBoxing`을 제공한다. 이 기능은 각 Wrapper class 에 상응하는 Primitive data type 일 경우에만 가능하다.

```java
List<Integer> lists = new ArrayList<>();
lists.add(1);
```

우린 `Integer`라는 Wrapper class 로 설정한 collection 에 데이터를 add 할 때 Integer 객체로 감싸서 넣지 않는다. 자바 내부에서 `AutoBoxing`해주기 때문이다.

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-2-1-java)

</br>

## Multi-Thread 환경에서의 개발

개발을 시작하는 입장에서 멀티 스레드를 고려한 프로그램을 작성할 일이 별로 없고 실제로 부딪히기 힘든 문제이기 때문에 많은 입문자들이 잘 모르고 있는 부분 중 하나라고 생각한다. 하지만 이 부분은 정말 중요하며 고려하지 않았을 경우 엄청난 버그를 양산할 수 있기 때문에 정말 중요하다.

### Field member

`필드(field)`란 클래스에 변수를 정의하는 공간을 의미한다. 이곳에 변수를 만들어두면 메소드 끼리 변수를 주고 받는 데 있어서 참조하기 쉬우므로 정말 편리한 공간 중 하나이다. 하지만 객체가 여러 스레드가 접근하는 싱글톤 객체라면 field 에서 상태값을 갖고 있으면 안된다. 모든 변수를 parameter 로 넘겨받고 return 하는 방식으로 코드를 구성해야 한다.

</br>

### 동기화(Synchronized)

`synchronized` 키워드를 직접 사용해서 특정 메소드나 구간에 Lock을 걸어 스레드 간 상호 배제를 구현할 수 있는 이 때 메서드에 직접 걸 수 도 있으며 블록으로 구간을 직접 지정해줄 수 있다.
메서드에 직접 걸어줄 경우에는 해당 class 인스턴스에 대해 Lock을 걸고 synchronized 블록을 이용할 경우에는 블록으로 감싸진 구간만 Lock이 걸린다. 때문에 Lock을 걸 때에는
이 개념에 대해 충분히 고민해보고 적절하게 사용해야만 한다.

그렇다면 필드에 Collection 이 불가피하게 필요할 때는 어떠한 방법을 사용할까? `synchronized` 키워드를 기반으로 구현된 Collection 들도 많이 존재한다. `List`를 대신하여 `Vector`를 사용할 수 있고, `Map`을 대신하여 `HashTable`을 사용할 수 있다. 하지만 이 Collection 들은 제공하는 API 가 적고 성능도 좋지 않다.

기본적으로는 `Collections`라는 util 클래스에서 제공되는 static 메소드를 통해 이를 해결할 수 있다. `Collections.synchronizedList()`, `Collections.synchronizedSet()`, `Collections.synchronizedMap()` 등이 존재한다.
JDK 1.7 부터는 `concurrent package`를 통해 `ConcurrentHashMap`이라는 구현체를 제공한다. Collections util 을 사용하는 것보다 `synchronized` 키워드가 적용된 범위가 좁아서 보다 좋은 성능을 낼 수 있는 자료구조이다.

</br>

### ThreadLocal

스레드 사이에 간섭이 없어야 하는 데이터에 사용한다. 멀티스레드 환경에서는 클래스의 필드에 멤버를 추가할 수 없고 매개변수로 넘겨받아야 하기 때문이다. 즉, 스레드 내부의 싱글톤을 사용하기 위해 사용한다. 주로 사용자 인증, 세션 정보, 트랜잭션 컨텍스트에 사용한다.

스레드 풀 환경에서 ThreadLocal 을 사용하는 경우 ThreadLocal 변수에 보관된 데이터의 사용이 끝나면 반드시 해당 데이터를 삭제해 주어야 한다. 그렇지 않을 경우 재사용되는 쓰레드가 올바르지 않은 데이터를 참조할 수 있다.

_ThreadLocal 을 사용하는 방법은 간단하다._

1.  ThreadLocal 객체를 생성한다.
2.  ThreadLocal.set() 메서드를 이용해서 현재 스레드의 로컬 변수에 값을 저장한다.
3.  ThreadLocal.get() 메서드를 이용해서 현재 스레드의 로컬 변수 값을 읽어온다.
4.  ThreadLocal.remove() 메서드를 이용해서 현재 스레드의 로컬 변수 값을 삭제한다.

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-2-1-java)

</br>

#### Personal Recommendation

* (도서) [Effective Java 2nd Edition](http://www.yes24.com/24/goods/14283616?scode=032&OzSrank=9)
* (도서) [스프링 입문을 위한 자바 객체 지향의 원리와 이해](http://www.yes24.com/24/Goods/17350624?Acode=101)

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-2-1-java)

</br>

</br>

_Java.end_
