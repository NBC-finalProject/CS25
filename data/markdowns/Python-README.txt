ipedia.org/wiki/Duck_test)


[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-2-3-python)

</br>

## Timsort : Python의 내부 sort

python의 내부 sort는 timsort 알고리즘으로 구현되어있다.
2.3 버전부터 적용되었으며, merge sort와 insert sort가 병합된 형태의 안정정렬이다. 

timsort는 merge sort의 최악 시간 복잡도와 insert sort의 최고 시간 복잡도를 보장한다. 따라서 O(n) ~ O(n log n)의 시간복잡도를 보장받을 수 있고, 공간복잡도의 경우에도 최악의 경우 O(n)의 공간복잡도를 가진다. 또한 안정정렬으로 동일한 키를 가진 요소들의 순서가 섞이지 않고 보장된다.

timsort를 좀 더 자세하게 이해하고 싶다면 [python listsort](https://github.com/python/cpython/blob/24e5ad4689de9adc8e4a7d8c08fe400dcea668e6/Objects/listsort.txt) 참고.

#### Reference

* [python listsort](https://github.com/python/cpython/blob/24e5ad4689de9adc8e4a7d8c08fe400dcea668e6/Objects/listsort.txt)
* [Timsort wikipedia](https://en.wikipedia.org/wiki/Timsort)

[뒤로](https://github.com/JaeYeopHan/for_beginner)/[위로](#part-2-3-python)

_Python.end_
