<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/commons/global.jsp" %>
<script type="text/javascript">
    var experiment_stu_experiment_stuDataGrid;
    var experiment_stu_experimentTree;
    var current_experiment_no = -1;
    var current_organization_id = -1;

    $(function() {
        experiment_stu_experimentTree = $('#experiment_stu_experimentTree').tree({
            url : '${path }/dbexp/tree',
            parentField : 'pid',
            lines : true,
            onSelect : function(node) {
                current_experiment_no = node.id;
                experiment_stu_experiment_stuDataGrid.datagrid({
                    url : '${path }/dbexperiment_stu/experimentStuByExpno',
                    queryParams:{
                            expno: node.id,
                            organization_id : current_organization_id
                        }
                });

            },
            onLoadSuccess:function(node,data){
               $("#experiment_stu_experimentTree li:eq(0)").find("div").addClass("tree-node-selected");   //设置第一个节点高亮
               var n = $("#experiment_stu_experimentTree").tree("getSelected");
               if(n!=null){
                    $("#experiment_stu_experimentTree").tree("select",n.target);    //相当于默认点击了一下第一个节点，执行onSelect方法
               }
            }
        });

        experiment_stu_experiment_stuDataGrid = $('#experiment_stu_experiment_stuDataGrid').datagrid({
            /*加载数据时指定 */
            //url : '${path }/dbexperiment_stu/experimentStuByExpno',
            striped : true,
            rownumbers : true,
            pagination : true,
            singleSelect : false,
            idField : 'expstuno',
            sortName : 'login_name',
            sortOrder : 'asc',
            pageSize : 20,
            pageList : [ 10, 20, 30, 40, 50, 100, 200, 300, 400, 500 ],
            frozenColumns : [ [  {
                field: 'expstuno',
                checkbox: true,
            },{
                width : '100',
                title : '学号',
                field : 'login_name',
                sortable : true
            } ,{
                   width : '100',
                   title : '姓名',
                   field : 'name',
                   sortable : true
               } ,  {
                  width:'100',
                  title : '提交时间',
                  field : 'submittime',
                  sortable : true
               },{
                 width : '100',
                 title : '测试描述',
                 field : 'testdesc',
                 sortable : true,
                 formatter : function(value, row, index){
                    var str=  "";
                    if (row.teststatus >= 2){
                        str = $.formatString("<a href='javascript:void(0)' onclick='open_test_log_experiment_stu_fun({0});' >{1}</a>", row.expstuno, value);
                        return str;
                    }else{
                        return value;
                    }
                 }
             } ,  {
                  width : '200',
                  title : '相似度描述',
                  field : 'simdesc',
                  sortable : true,
                  formatter : function(value, row, index){
                          if (row.simstatus == 0){
                              return "正常";
                          }else{
                              return value;
                          }
                  }
              }, {
                field : 'teststatus',
                title : '操作',
                width : 200,
                formatter : function(value, row, index) {
                    var str = '';
                    if (value == 0){
                        str = "正在测试..."
                    } else if (value == -1){
                        str = "未测试";
                    }else{
                        str = "测试完成";
                    }
                    return str;
                }
            } ] ],
            toolbar : '#experiment_stuToolbar'
        });

        /*选择班级时触发的事件*/
        $("#organizationid").combobox({
            onSelect:function(record){
                current_organization_id = record.value;
                experiment_stu_experiment_stuDataGrid.datagrid({
                    url : '${path }/dbexperiment_stu/experimentStuByExpno',
                    queryParams:{
                            expno: current_experiment_no,
                            organization_id : record.value
                        }
                });
            }
		});
    });

    function test_experiment_stuFun() {

        var rows = experiment_stu_experiment_stuDataGrid.datagrid('getSelections');

        var expstunos = [];
        if (rows && rows.length > 0) {
            for ( var i = 0; i < rows.length; i++) {
                expstunos.push(rows[i].expstuno);
            }
        }
        var checkBatchController = "${path }/dbexperiment_stu/checkBatch";

        var form = new FormData();
        form.append("expstunos", expstunos);

        // XMLHttpRequest 对象
        var xhr = new XMLHttpRequest();
        xhr.open("post", checkBatchController, false);

        xhr.send(form);
    }

    function open_test_log_experiment_stu_fun(expstuno){
        parent.$.modalDialog({
                    title : '查看测试日志',
                    width : 900,
                    height : 700,
                    href : '${path }/dbexperiment_stu/openTestLogPage?expstuno=' + expstuno,
                    buttons : [ {
                        text : '关闭',
                        handler : function() {
                            parent.$.modalDialog.handler.dialog('close');
                        }
                    } ]
                });
    }



</script>
<div class="easyui-layout" data-options="fit:true,border:false">
    <div data-options="region:'west',border:true,split:false,title:'课程设计实验列表'"  style="width:300px;overflow: hidden; ">
        <ul id="experiment_stu_experimentTree" style="width:160px;margin: 10px 10px 10px 10px"></ul>
    </div>
    <div data-options="region:'center',border:true,title:'选此实验的学生'" >
        <table id="experiment_stu_experiment_stuDataGrid" data-options="fit:true,border:false"></table>
    </div>

</div>

<div id="experiment_stuToolbar" style="display: none;">
选择班级<select id="organizationid" name="organizationid"  onchange="organization_change()" class="easyui-combobox" data-options="width:200,height:29,editable:false,panelHeight:'auto'">
    <option value='-1'>全部</option>
    <c:forEach items="${organizations}" var="organization" >
        <option value="${organization.id}">${organization.name}</option>
    </c:forEach>
</select>

    <shiro:hasPermission name="/dbexpfiles/add">
        <a onclick="test_experiment_stuFun();" href="javascript:void(0);" class="easyui-linkbutton" data-options="plain:true,iconCls:'fi-plus icon-green'">测试选中作业</a>
    </shiro:hasPermission>

</div>