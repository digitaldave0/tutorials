
​def groupListFromMap(map, key) {   
  def groupedList = []   
  map.each { k, v ->     
    groupedList.add([key: k, value: v])   
  }   
  return groupedList }

​def map = [   'key1': ['item1', 'item2'],   
              'key2': ['item3', 'item4']
]  

def groupedList = groupListFromMap(map, 'key')

​println groupedList
