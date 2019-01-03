<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/commons/global.jsp" %>
<script type="text/javascript">
    var  experiment_expstu_submitDataGrid;
    $(function() {
        experiment_expstu_submitDataGrid = $('#experiment_expstu_submitDataGrid').datagrid({
                        url : '${path }/dbexperiment_stu/experimentFilesDataGrid?expstuno=${expstuno}',
                        striped : true,
                        rownumbers : true,
                        pagination : true,
                        singleSelect : false,
                        idField : 'fileno',
                        sortName : 'fileno',
                        sortOrder : 'asc',
                        pageSize : 20,
                        frozenColumns : [ [{
                            width : '300',
                            title : '文件名称',
                            field : 'srcfilename',
                            sortable : true
                        },{
                            width : '100',
                            title: '上传状态',
                            field: 'desc'
                         },{
                           width: '100',
                           title: '文件大小',
                           field: 'size'
                         },  {
                             field : 'status',
                             title : '操作',
                             width : 200,
                             formatter : function(value, row, index) {
                                 var str = '';
                                 if (value == 1){
                                    str += $.formatString('<a href="javascript:void(0)" class="experiment_stu-easyui-linkbutton-submit-cancel" data-options="plain:true,iconCls:\'fi-pencil icon-blue\'" onclick="cancel_submit_files_experiment_stuFun(\'{0}\');" >取消</a>', index);
                                 }
                                 return str;
                             }
                         } ] ],
                      onLoadSuccess:function(data){
                          $('.experiment_stu-easyui-linkbutton-submit-cancel').linkbutton({text:'取消'});
                      }
                    });
        $('#experiment_stuSubmitForm').form({
            url : '${path }/dbexperiment_stu/check',
            onSubmit : function() {
                progressLoad();


                var isValid = $(this).form('validate');
                if (!isValid) {
                    progressClose();
                }

                return isValid;
            },
            success : function(result) {
                progressClose();
                result = $.parseJSON(result);
                if (result.success) {
                    parent.$.modalDialog.openner_dataGrid.datagrid('reload');//之所以能在这里调用到parent.$.modalDialog.openner_dataGrid这个对象，是因为user.jsp页面预定义好了
                    parent.$.modalDialog.handler.dialog('close');
                } else {
                    var form = $('#experiment_stuSubmitForm');
                    parent.$.messager.alert('错误', eval(result.msg), 'error');
                }
            }
        });

    });


    function cancel_submit_files_experiment_stuFun(rowindex) {
        $("#experiment_expstu_submitDataGrid").datagrid("updateRow",{
                index: parseInt(rowindex),
                row:{
                        desc: '',
                        size: '',
                        status : 0
                }
        });

    }

    var uploadFiles = new Array();
$(document).on({
            dragleave:function(e){    //拖离
                e.preventDefault();
            },
            drop:function(e){  //拖后放
                e.preventDefault();
            },
            dragenter:function(e){    //拖进
                e.preventDefault();
            },
            dragover:function(e){    //拖来拖去
                e.preventDefault();
            }
        });

        var box = document.getElementById('dropbox'); //拖拽区域

        box.addEventListener("drop",function(e){
            e.preventDefault(); //取消默认浏览器拖拽效果
            var fileList = e.dataTransfer.files; //获取文件对象
            //检测是否是拖拽文件到页面的操作
            if(fileList.length == 0){
                return false;
            }
            AddFiles(fileList);
        },false);

function AddFiles(files){
        var errstr = "";
        for(var i=0; i< files.length; i++){
            var filename = files[i].name;
            var isfind = false;
            for(var j=0; j< uploadFiles.length; j++){
                if(uploadFiles[j].name == filename){
                    isfind = true;
                    break;
                }
            }

            var index1=filename.lastIndexOf(".");
            var index2=filename.length;
            var postf=filename.substring(index1+1,index2);//后缀名
            var myarray = new Array('CPP','cpp','C','c');

            if($.inArray(postf,myarray) == -1){
                errstr += filename + "/";
                continue;
            }
            if(isfind == false){
                uploadFiles.push(files[i]);
            }
            updateDataGrid(files[i])
        }

        if(errstr != ""){
            alert("文件格式错误:"+errstr);
        }

    }

    function updateDataGrid(afile){
        var filename = afile.name.toLowerCase();
        var size = (afile.size / 1000) + "k"
        var info=$("#experiment_expstu_submitDataGrid").datagrid("getData");
		var total=0;
		for(var i=0;i<info.rows.length;i++){
		    var fn = info.rows[i].srcfilename.toLowerCase();
		    if(filename == fn){
                $("#experiment_expstu_submitDataGrid").datagrid("updateRow",{
                        index:i,
                        row:{
                                desc:'待上传',
                                size: size,
                                status : 1
                        }
                });
                break;
		    }
		}
    }

 function onc(){
        var files = document.getElementById("file").files;

        if(files.length < 0){
            return ;
        }
        AddFiles(files);
    }
function Upload(){
/*
        AddFiles(new Array());
        if(uploadFiles.length <= 0){
            Refresh();
            return;
        }

        uploadcount = uploadFiles.length ;

        var FileController = "SaveFile.php";                    // 接收上传文件的后台地址
        // FormData 对象

        var form = new FormData();
        form.append("file", uploadFiles[0]);
        // XMLHttpRequest 对象
        var xhr = new XMLHttpRequest();
        xhr.open("post", FileController, true);
        xhr.onload = function () {
            Upload();
        };
        xhr.send(form);
        uploadFiles.splice(0,1);
        */
    }
</script>
<div class="easyui-layout" data-options="fit:true,border:false" >
    <div data-options="region:'center',border:false" style="overflow: hidden;padding: 3px;" >
        <table id="experiment_expstu_submitDataGrid" data-options="fit:true,border:false"></table>


        <form id="experiment_stuSubmitForm" method="post">
            <input name="expnos" id="expnos" type="hidden"  value="">
        </form>
    </div>
    <div name="dropbox" id="dropbox" data-options="region:'south',border:false"  style="background-color:#888888; color:white; font-size:20px; height:120px;line-height:110px;border:3px dashed silver;text-align: center;display:block;">
       拖拽文件到此处准备上传
    </div>
</div>