 합이 최소인 `spanning tree`를 말한다. 여기서 말하는 `spanning tree`란 그래프 G 의 모든 vertex 가 cycle 이 없이 연결된 형태를 말한다.

### Kruskal Algorithm

초기화 작업으로 **edge 없이** vertex 들만으로 그래프를 구성한다. 그리고 weight 가 제일 작은 edge 부터 검토한다. 그러기 위해선 Edge Set 을 non-decreasing 으로 sorting 해야 한다. 그리고 가장 작은 weight 에 해당하는 edge 를 추가하는데 추가할 때 그래프에 cycle 이 생기지 않는 경우에만 추가한다. spanning tree 가 완성되면 모든 vertex 들이 연결된 상태로 종료가 되고 완성될 수 없는 그래프에 대해서는 모든 edge 에 대해 판단이 이루어지면 종료된다.
[Kruskal Algorithm의 세부 동작과정](https://gmlwjd9405.github.io/2018/08/29/algorithm-kruskal-mst.html)
[Kruskal Algorithm 관련 Code](https://github.com/morecreativa/Algorithm_Practice/blob/master/Kruskal%20Algorithm.cpp)

#### 어떻게 cycle 생성 여부를 판단하는가?

Graph 의 각 vertex 에 `set-id`라는 것을 추가적으로 부여한다. 그리고 초기화 과정에서 모두 1~n 까지의 값으로 각각의 vertex 들을 초기화 한다. 여기서 0 은 어떠한 edge 와도 연결되지 않았음을 의미하게 된다. 그리고 연결할 때마다 `set-id`를 하나로 통일시키는데, 값이 동일한 `set-id` 개수가 많은 `set-id` 값으로 통일시킨다.

#### Time Complexity

1.  Edge 의 weight 를 기준으로 sorting - O(E log E)
2.  cycle 생성 여부를 검사하고 set-id 를 통일 - O(E + V log V)
    => 전체 시간 복잡도 : O(E log E)

### Prim Algorithm

초기화 과정에서 한 개의 vertex 로 이루어진 초기 그래프 A 를 구성한다. 그리고나서 그래프 A 내부에 있는 vertex 로부터 외부에 있는 vertex 사이의 edge 를 연결하는데 그 중 가장 작은 weight 의 edge 를 통해 연결되는 vertex 를 추가한다. 어떤 vertex 건 간에 상관없이 edge 의 weight 를 기준으로 연결하는 것이다. 이렇게 연결된 vertex 는 그래프 A 에 포함된다. 위 과정을 반복하고 모든 vertex 들이 연결되면 종료한다.

#### Time Complexity

=> 전체 시간 복잡도 : O(E log V)

</br>

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-1-2-datastructure)

_DataStructure.end_
