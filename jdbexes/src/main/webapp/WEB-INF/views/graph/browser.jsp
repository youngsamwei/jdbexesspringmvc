<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/commons/global.jsp" %>

<script type="text/javascript" src="${staticPath }/static/neo4j_browser/vis.js" charset="utf-8"></script>
<script type="text/javascript" src="${staticPath }/static/neo4j_browser/layer.js" charset="utf-8"></script>
<link rel="stylesheet" type="text/css" href="${staticPath }/static/style/css/vis-network.min.css" />

<style type="text/css">
    .network {
        width: 99%;
        height: 96%;
        border: 1px solid lightgray;
        background-color: #fff;
    }
</style>

<!--
显示节点
<input type="checkbox" name="checkbox" checked value="Person"/>Person
<input type="checkbox" name="checkbox" checked value="Movie"/>Movie
-->

<a onclick="findSimilaritiesBySimValue();" href="javascript:void(0);" class="easyui-linkbutton" data-options="plain:true,iconCls:'fi-page-search icon-green'">查询</a>
相似度大于等于
<input name="simValue" id="simValue" type="text" placeholder="请输入相似度" class="easyui-numberbox span2" style="width: 50px; height: 20px;" data-options="required:true" value="100"></td>
选择实验<select id="expid" name="expid" class="easyui-combobox" data-options="width:200,height:29,editable:false,panelHeight:'auto'">
    <option>全部</option>
    <c:forEach items="${experiments}" var="experiment" >
        <option value="${experiment.experimentid}">${experiment.name}</option>
    </c:forEach>
</select>

选择学生
<select id="stuid" name="stuid" class="easyui-combobox" data-options="width:200,height:29,editable:false,panelHeight:200">
    <option>全部</option>
    <c:forEach items="${students}" var="student" >
        <option value="${student.studentid}">${student.sno}_${student.name}</option>
    </c:forEach>
</select>

<div id="network_id" class="network" class="easyui-layout" data-options="fit:true,border:false"></div><!-- 拓扑图容器-->
<script>
    //拓扑
    var network;
    // 创建节点对象
    var nodes;
    // 创建连线对象
    var edges;
    // 已扩展的节点
    var nodeExtendArr = new Array();
    //所有的实验集合
    var exps = [];

    $(function () {
        progressLoad();

        init();
        //修改初始缩放
        network.moveTo({scale: 0.8});
        //先初始化一个节点

        /* 需要增加 等待图标 */

        $.ajax({
            url:'/graph/getSimilaritiesBySimValueExperimentidStudentid',
            async:true,
            data:{
                simValue : 100,
                expid:6,
                stuid : 1
            },
            success: function(ret) {
                if(ret){
                    var result = $.parseJSON(ret);
                    /*console.info(result);*/
                    nodes.clear();
                    edges.clear();
                    createSimilarityNetwork(result);
                    /*createNetwork(result);*/
                }else{
                    layer.msg("查询失败");
                }
                progressClose();
            }
        });

        network.on("dragEnd", function(params){
            if (params.nodes&&params.nodes.length > 0){
                network.clustering.updateClusteredNode(params.nodes[0], {physics : false});
            }
        });
        //双击扩展
        network.on("doubleClick",function (params) {
            // 取出当前节点在Vis的节点ID
            var nodeId = params.nodes[0];
            if(nodeExtendArr.indexOf(nodeId) != -1){
                layer.msg("该节点已经扩展");
            }else{
                getData(nodeId);
            }
        });
    });

    function findSimilaritiesBySimValue(){
        progressLoad();
        var simValue = $('#simValue').numberbox('getValue');
        var expid = $('#expid').combobox('getValue');
        var stuid = $('#stuid').combobox('getValue');
        var url = 'getSimilarities';
        var data = {simValue : simValue};
        if (expid == '全部' ){
            if (stuid == '全部'){
                url = 'getSimilarities';
            }else{
                url = '/graph/getSimilaritiesBySimValueStudentid';
                data.stuid = stuid;
            }
        }else{
            if (stuid == '全部'){
                url = '/graph/getSimilaritiesBySimValueExperimentid';
                data.expid = expid;
            }else{
                url = '/graph/getSimilaritiesBySimValueExperimentidStudentid';
                data.expid = expid;
                data.stuid = stuid;
            }
        }
        $.ajax({
            url: url,
            async:true,
            data: data,
            success: function(ret) {
                if(ret){
                    var result = $.parseJSON(ret);
                    /*console.info(result);*/
                    nodes.clear();
                    edges.clear();
                    createSimilarityNetwork(result);
                }else{
                    layer.msg("查询失败");
                }
                progressClose();
            }
        });
    }

    function init(){
        // 创建节点对象
        nodes = new vis.DataSet([]);
        // 创建连线对象
        edges = new vis.DataSet([]);
        // 创建一个网络拓扑图  不要使用jquery获取元素
        var container = document.getElementById('network_id');
        var data = {nodes: nodes, edges: edges};
        //全局设置，每个节点和关系的属性会覆盖全局设置
        var options = {
            //设置节点形状
            nodes:{
                shape: 'dot',//采用远点的形式
                size: 30,
                font:{
                    face:'Microsoft YaHei'
                }
            },
            // 设置关系连线
            edges:{
                font:{
                    face:'Microsoft YaHei'
                }
            },
            //设置节点的相互作用
            interaction: {
                //鼠标经过改变样式
                hover: true
                //设置禁止缩放
                //zoomView:false
            },
            //力导向图效果
            physics: {
                enabled: true,
                barnesHut: {
                    gravitationalConstant: -4000,
                    centralGravity: 0.3,
                    springLength: 120,
                    springConstant: 0.04,
                    damping: 0.09,
                    avoidOverlap: 0
                }
            }
        };
        network = new vis.Network(container, data, options);
    }

    //获取id扩展后的数据
    function getData(id){
        var tipMsg = layer.msg('数据加载中，请稍等...', {icon: 16,shade:[0.1,'#000'],time:0,offset:'250px'});
        //该节点已扩展
        nodeExtendArr.push(id);
        $.ajax({
            url:'/getPath',
            data:{
                id:id //当前节点id
            },
            success: function(ret) {
                layer.close(tipMsg);
                if(ret){
                    createNetwork({nodes:ret.nodeList,edges:ret.edgeList});
                }else{
                    layer.msg("查询失败");
                }
            }
        });
    }

    function experiment_exists(exps, exp){
        for (var i = 0; i < exps.length; i++){
            if (exps[i].id == exp.id){
                return true;
            }
        }
        return false;
    }

    function processAssignment(assignment){
        if (assignment.students){
            var student  = assignment.students[0];
            if(!node_exists(student)){
                nodes.add({
                    id : student.id,
                    type : 'student',
                    label : student.name
                });
            }
            edges.add({
                 /*id: edge.edgeId,*/
                 arrows: 'to',
                 from: student.id,
                 to: assignment.id,
                 /*label: sim.simValue,*/
                 font: {align: "middle"},
                 length: 150
            });
        }
    }

    function createSimilarityNetwork(sims){
        for (var i = 0; i < sims.length; i++){
            var sim = sims[i];
            var a1 = sim.a1;
            var a2 = sim.a2;
            if (!node_exists(a1)){
                nodes.add({
                    id : a1.id,
                    type : 'assignment',
                    label : a1.submitDate,
                    color:{
                        background: '#FFD86E'
                    }
                });
                processAssignment(a1);
            }
            if (!node_exists(a2)){
                nodes.add({
                    id : a2.id,
                    type : 'assignment',
                    label : a2.submitDate,
                    color:{
                        background: '#FFD86E'
                    }
                });
                processAssignment(a2);
            }
            var value = sim.simValue.toFixed(2) + "";
            var length = 150 - sim.simValue.toFixed(0);
            edges.add({
                /*id: edge.edgeId,*/
                arrows: 'to',
                from: a1.id,
                to: a2.id,
                label: value,
                font: {align: "middle"},
                length: length
            });
        }
        get_key_node();
    }

    /*查找只有入度没有出度的作业节点，并将其背景设为红色*/
    function get_key_node(){
        var key_node = new Map();
        var non_key_node = new Map();
        var ids = edges.getIds();
        for(var i = 0; i < ids.length; i++){
            var edge = edges.get(ids[i]);
            var node = nodes.get(edge.from);
            if (node.type == 'assignment'){
                non_key_node.set(edge.from, edge.from);
                key_node.set(edge.to, edge.to);
            }
        }
        non_key_node.forEach(function (item, key, mapObj) {
            key_node.delete(key);
        });

        key_node.forEach(function (item, key, mapObj){
            var node = nodes.get(key);
            node.color.background = '#FF0000';
            nodes.update(node);
        });

    }

    function node_exists(node){
        return nodes.get(node.id);
    }

    //扩展节点 param nodes和relation集合
    function createNetwork(param) {
        for (var i = 0; i < param.length; i++){
            var node = param[i];
            nodes.add({
                id: node.id,
                label: node.name,
                color:{
                    background: '#FFD86E'
                }
            });
            if (node.assignments && node.assignments.length>0){
                var assignments = node.assignments;

                for (var j = 0; j < assignments.length; j++){
                    nodes.add({
                        id: assignments[j].id,
                        label : assignments[j].submitDate,
                        color:{
                            background: '#6DCE9E'
                        }
                    });
                    edges.add({
                        /*id: edge.edgeId,*/
                        arrows: 'to',
                        from: node.id,
                        to: assignments[j].id,
                        /*label: label,*/
                        font: {align: "middle"},
                        length: 150
                    });
                    for (var k = 0; k < assignments[j].experiments.length; k++){
                        if (!experiment_exists(exps, assignments[j].experiments[k])){
                            exps.push(assignments[j].experiments[k]);
                            nodes.add({
                            id:assignments[j].experiments[k].id,
                            lable:assignments[j].experiments[k].name

                            });
                        }
                        edges.add({
                            arrows:'to',
                            from: assignments[j].experiments[k].id,
                            to:assignments[j].id,
                            font: {align: "middle"},
                            length: 150
                        });

                    }
                }

            }
        }

    }

    $('input[type=checkbox][name=checkbox]').change(function(e) {
        for(var i in network.body.data.nodes._data){
            if(network.body.data.nodes._data[i].label.indexOf("title")!=-1 && e.target.value == "Movie" && !e.currentTarget.checked){
                network.clustering.updateClusteredNode(i, {hidden : true});
            }else if(network.body.data.nodes._data[i].label.indexOf("name")!=-1 && e.target.value == "Person" && !e.currentTarget.checked){
                network.clustering.updateClusteredNode(i, {hidden : true});
            }else{
                network.clustering.updateClusteredNode(i, {hidden : false});
            }
        }
    });

    //根据对象组数中的某个属性值进行过滤删除
    //arrName数组名  field过滤的字段   keyValue字段值
    function deleteValueFromArr(arrName,field,keyValue){
        if(arrName==null || arrName.length==0){
            return null;
        }
        for (var i =0;i< arrName.length;i++){
            if(arrName[i][field]==keyValue){
                arrName.splice(i,1);
            }
        }
        return arrName;
    }
    //根据对象数组中的某个属性值获取过滤后的数组
    //arrName数组名  field过滤的字段   keyValue字段值
    function getArrFromArr(arrName,field,keyValue){
        var arrReturn=[];
        if(arrName==null || arrName.length==0){
            return arrReturn;
        }
        var obj;
        for (var item=0; item< arrName.length;item++){
            obj=arrName[item];
            if(obj[field]==keyValue){
                arrReturn.push(obj);
            }
        }
        return arrReturn;
    }

</script>