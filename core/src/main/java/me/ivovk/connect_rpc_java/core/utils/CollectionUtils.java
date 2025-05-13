package me.ivovk.connect_rpc_java.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionUtils {

  public static <E> Collection<E> merge(Collection<E> list1, Collection<E> list2) {
    if (list1 == null && list2 == null) {
      return null;
    }
    if (list1 == null) {
      return list2;
    }
    if (list2 == null) {
      return list1;
    }

    var capacity = list1.size() + list2.size();

    List<E> mergedList = new ArrayList<>(capacity);
    mergedList.addAll(list1);
    mergedList.addAll(list2);
    return Collections.unmodifiableList(mergedList);
  }
}
