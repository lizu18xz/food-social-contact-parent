# food-social-contact-parent
结论：

1.join 的where 和on没有区别，内链接，只有复核条件的才会显示出来，条件加在那里都可以

2.left join 的where和on有区别，left join 不管on的条件是否匹配都是显示左表的全部内容，然后形成虚拟表，通过where去筛选临时表中满足条件的记录

如果筛选条件在on就会造成匹配不到的数据，左侧还是左表的内容，右边全是null而忽略链接条件 

left join 筛选的主要是右表，如果筛选不到就会在临时表中显示null，这里on加的是链接条件，筛选条件在where中

 

where不能放函数，函数条件需要放在having中