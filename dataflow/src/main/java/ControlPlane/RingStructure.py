import matplotlib.pyplot as plt

fig, ax = plt.subplots()

# labels1 = ['Worker2:2', 'Worker1:1']
# points1 = [101, 99]
# ax.pie(points1, labels = labels1, autopct = '%1.1f%%')

# labels2 = ['Worker2:2', 'Worker1:1', 'Worker3:9']
# points2 = [46, 82, 72]
# ax.pie(points2, labels = labels2, autopct = '%1.1f%%')

# labels3 = ['Worker2:2', 'Worker1:1', 'Worker5:8', 'Worker6:0', 'Worker3:9', 'Worker4:5']
# points3 = [44, 52, 24, 26, 37, 17]
# ax.pie(points3, labels = labels3, autopct = '%1.1f%%')

labels4 = ['Worker2:2', 'Worker8:4', 'Worker1:1', 'Worker5:8', 'Worker6:0', 'Worker3:9', 'Worker9:5', 'Worker10:4', 'Worker11:3', 'Worker7:2', 'Worker12:3', 'Worker4:5']
points4 = [18, 10, 26, 21, 4, 14, 13, 9, 18, 27, 26, 14]
ax.pie(points4, labels = labels4, autopct = '%1.1f%%')
plt.show()
