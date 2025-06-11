기존의 function 표현방식보다 간결하게 함수를 표현할 수 있다. 화살표 함수는 항상 익명이며, 자신의 this, arguments, super 그리고 new.target을 바인딩하지 않는다. 그래서 생성자로는 사용할 수 없다.
- 화살표 함수 도입 영향: 짧은 함수, 상위 스코프 this

### 짧은 함수
```js
var materials = [
  'Hydrogen',
  'Helium',
  'Lithium',
  'Beryllium'
];

materials.map(function(material) { 
  return material.length; 
}); // [8, 6, 7, 9]

materials.map((material) => {
  return material.length;
}); // [8, 6, 7, 9]

materials.map(({length}) => length); // [8, 6, 7, 9]
```
기존의 function을 생략 후 => 로 대체 표현

### 상위 스코프 this
```js
function Person(){
  this.age = 0;

  setInterval(() => {
    this.age++; // |this|는 person 객체를 참조
  }, 1000);
}

var p = new Person();
```
일반 함수에서 this는 자기 자신을 this로 정의한다. 하지만 화살표 함수 this는 Person의 this와 동일한 값을 갖는다. setInterval로 전달된 this는 Person의 this를 가리키며, Person 객체의 age에 접근한다.

#### Reference

* https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Functions/Arrow_functions

</br>

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-2-2-javascript)

</br>

=====
_JavaScript.end_
