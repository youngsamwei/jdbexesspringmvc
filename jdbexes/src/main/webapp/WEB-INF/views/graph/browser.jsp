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

显示节点
<input type="checkbox" name="checkbox" checked value="Person"/>Person
<input type="checkbox" name="checkbox" checked value="Movie"/>Movie
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
        init();
        //修改初始缩放
        network.moveTo({scale: 0.8});
        //先初始化一个节点
        $.ajax({
            url:'/graph/getStudents',
            async:false,
            success: function(ret) {
                if(ret){
                    var result = $.parseJSON(ret);
                    createNetwork(result);
                }else{
                    layer.msg("查询失败");
                }
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