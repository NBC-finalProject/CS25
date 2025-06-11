 것

### 5. 빠른 자바스크립트 코드를 작성하자

* 코드를 최소화할 것
* 필요할 때만 스크립트를 가져올 것 : flag 사용
* DOM 에 대한 접근을 최소화 할 것 : Dom manipulate 는 느리다.
* 다수의 엘리먼트를 찾을 때는 selector api 를 사용할 것.
* 마크업의 변경은 한번에 할 것 : temp 변수를 활용
* DOM 의 크기를 작게 유지할 것.
* 내장 JSON 메서드를 사용할 것.

### 6. 애플리케이션의 작동원리를 알고 있자.

* Timer 사용에 유의할 것.
* `requestAnimationFrame` 을 사용할 것
* 활성화될 때를 알고 있을 것

#### Reference

* [HTML5 앱과 웹사이트를 보다 빠르게 하는 50 가지 - yongwoo Jeon](https://www.slideshare.net/mixed/html5-50)

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-3-1-front-end)

</br>

## 서버 사이드 렌더링 vs 클라이언트 사이드 렌더링

* 그림과 함께 설명하기 위해 일단 블로그 링크를 추가한다.
* http://asfirstalways.tistory.com/244

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-3-1-front-end)

</br>

## CSS Methodology

`SMACSS`, `OOCSS`, `BEM`에 대해서 소개한다.

### SMACSS(Scalable and Modular Architecture for CSS)

`SMACSS`의 핵심은 범주화이며(`categorization`) 스타일을 다섯 가지 유형으로 분류하고, 각 유형에 맞는 선택자(selector)와 작명법(naming convention)을 제시한다.

* 기초(Base)
  * element 스타일의 default 값을 지정해주는 것이다. 선택자로는 요소 선택자를 사용한다.
* 레이아웃(Layout)
  * 구성하고자 하는 페이지를 컴포넌트를 나누고 어떻게 위치해야하는지를 결정한다. `id`는 CSS 에서 클래스와 성능 차이가 없는데, CSS 에서 사용하게 되면 재사용성이 떨어지기 때문에 클래스를 주로 사용한다.
* 모듈(Module)
  * 레이아웃 요소 안에 들어가는 더 작은 부분들에 대한 스타일을 정의한다. 클래스 선택자를 사용하며 요소 선택자는 가급적 피한다. 클래스 이름은 적용되는 스타일의 내용을 담는다.
* 상태(States)
  * 다른 스타일에 덧붙이거나 덮어씌워서 상태를 나타낸다. 그렇기 때문에 자바스크립트에 의존하는 스타일이 된다. `is-` prefix 를 붙여 상태를 제어하는 스타일임을 나타낸다. 특정 모듈에 한정된 상태는 모듈 이름도 이름에 포함시킨다.
* 테마(Theme)
  * 테마는 프로젝트에서 잘 사용되지 않는 카테고리이다. 사용자의 설정에 따라서 css 를 변경할 수 있는 css 를 설정할 때 사용하게 되며 접두어로는 `theme-`를 붙여 표시한다.

</br>

### OOCSS(Object Oriented CSS)

객체지향 CSS 방법론으로 2 가지 기본원칙을 갖고 있다.

* 원칙 1. 구조와 모양을 분리한다.
  * 반복적인 시각적 기능을 별도의 스킨으로 정의하여 다양한 객체와 혼합해 중복코드를 없앤다.
* 원칙 2. 컨테이너와 컨텐츠를 분리한다.
  * 스타일을 정의할 때 위치에 의존적인 스타일을 사용하지 않는다. 사물의 모양은 어디에 위치하든지 동일하게 보여야 한다.

</br>

### BEM(Block Element Modifier)

웹 페이지를 각각의 컴포넌트의 조합으로 바라보고 접근한 방법론이자 규칙(Rule)이다. SMACSS 가 가이드라인이라는 것에 비해서 좀 더 범위가 좁은 반면 강제성 측면에서 다소 강하다고 볼 수 있다. BEM 은 CSS 로 스타일을 입힐 때 id 를 사용하는 것을 막는다. 또한 요소 셀렉터를 통해서 직접 스타일을 적용하는 것도 불허한다. 하나를 더 불허하는데 그것은 바로 자손 선택자 사용이다. 이러한 규칙들은 재사용성을 높이기 위함이다.

* Naming Convention
  * 소문자와 숫자만을 이용해 작명하고 여러 단어의 조합은 하이픈(`-`)과 언더바(`_`)를 사용하여 연결한다.
* BEM 의 B 는 “Block”이다.
  * 블록(block)이란 재사용 할 수 있는 독립적인 페이지 구성 요소를 말하며, HTML 에서 블록은 class 로 표시된다. 블록은 주변 환경에 영향을 받지 않아야 하며, 여백이나 위치를 설정하면 안된다.
* BEM 의 E 는 “Element”이다.
  * 블록 안에서 특정 기능을 담당하는 부분으로 block_element 형태로 사용한다. 요소는 중첩해서 작성될 수 있다.
* BEM 의 M 는 “Modifier”이다.
  * 블록이나 요소의 모양, 상태를 정의한다. `block_element-modifier`, `block—modifier` 형태로 사용한다. 수식어에는 불리언 타입과 키-값 타입이 있다.

</br>

#### Reference

* [CSS 방법론에 대해서](http://wit.nts-corp.com/2015/04/16/3538)
* [CSS 방법론 SMACSS 에 대해 알아보자](https://brunch.co.kr/@larklark/1)
* [BEM 에 대해서](https://en.bem.info/)

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-3-1-front-end)

</br>

## normalize vs reset

브라우저마다 기본적으로 제공하는 element 의 style 을 통일시키기 위해 사용하는 두 `css`에 대해 알아본다.

### reset.css

`reset.css`는 기본적으로 제공되는 브라우저 스타일 전부를 **제거** 하기 위해 사용된다. `reset.css`가 적용되면 `<H1>~<H6>`, `<p>`, `<strong>`, `<em>` 등 과 같은 표준 요소는 완전히 똑같이 보이며 브라우저가 제공하는 기본적인 styling 이 전혀 없다.

### normalize.css

`normalize.css`는 브라우저 간 일관된 스타일링을 목표로 한다. `<H1>~<H6>`과 같은 요소는 브라우저간에 일관된 방식으로 굵게 표시됩니다. 추가적인 디자인에 필요한 style 만 CSS 로 작성해주면 된다.

즉, `normalize.css`는 모든 것을 "해제"하기보다는 유용한 기본값을 보존하는 것이다. 예를 들어, sup 또는 sub 와 같은 요소는 `normalize.css`가 적용된 후 바로 기대하는 스타일을 보여준다. 반면 `reset.css`를 포함하면 시각적으로 일반 텍스트와 구별 할 수 없다. 또한 normalize.css 는 reset.css 보다 넓은 범위를 가지고 있으며 HTML5 요소의 표시 설정, 양식 요소의 글꼴 상속 부족, pre-font 크기 렌더링 수정, IE9 의 SVG 오버플로 및 iOS 의 버튼 스타일링 버그 등에 대한 이슈를 해결해준다.

### 그 외 프론트엔드 개발 환경 관련

- 웹팩(webpack)이란?
  - 웹팩은 자바스크립트 애플리케이션을 위한 모듈 번들러입니다. 웹팩은 의존성을 관리하고, 여러 파일을 하나의 번들로 묶어주며, 코드를 최적화하고 압축하는 기능을 제공합니다.
  - https://joshua1988.github.io/webpack-guide/webpack/what-is-webpack.html#%EC%9B%B9%ED%8C%A9%EC%9D%B4%EB%9E%80
- 바벨과 폴리필이란?

  - 바벨(Babel)은 자바스크립트 코드를 변환해주는 트랜스 컴파일러입니다. 최신 자바스크립트 문법으로 작성된 코드를 예전 버전의 자바스크립트 문법으로 변환하여 호환성을 높이는 역할을 합니다.

    이 변환과정에서 브라우저별로 지원하는 기능을 체크하고 해당 기능을 대체하는 폴리필을 제공하여 이를 통해 크로스 브라우징 이슈도 어느정도 해결할 수 있습니다.

  - 폴리필(polyfill)은 현재 브라우저에서 지원하지 않는 최신기능이나 API를 구현하여, 오래된 브라우저에서도 해당 기능을 사용할 수 있도록 해주는 코드조각입니다.

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-3-1-front-end)

</br>

</br>

_Front-End.end_
