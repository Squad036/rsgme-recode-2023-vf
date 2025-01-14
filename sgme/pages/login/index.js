import Head from "next/head";
import Image from "next/image";
import Link from "next/link";
import {useForm} from "react-hook-form";
import {useRouter} from "next/router";
import {http} from "@/utils/http";
import React, {useState} from "react";
import {useAuth} from "@/context/authContext";
import axios from "axios";

function Index(props) {

    const router = useRouter();

    const {login, token} = useAuth();


    const {
        register,
        handleSubmit,
        formState: {errors}
    } = useForm();

    const [erroLogin, setErroLogin] = useState(false);
    const [erroLoginMessage, setErroLoginMessage] = useState(false);

    
    const onSubmit = async (data) => {
        const response = http.post(
            "http://localhost:8080/auth/login",
            data
        )
            .then((response) => {
                login(response.data)
                router.push("/gestao-sgme")
            })
            .catch((error) => {
                if (axios.isAxiosError(error) && error.response) {
                    setErroLogin(true)
                    setErroLoginMessage(error.response.data.message);
                } else {
                    setErroLogin(true)
                    setErroLoginMessage("Servidor indisponivel no momento")
                }
            });


    }

    return (
        <>
            <Head>
                <title>SGME - Login</title>
                <meta name="description" content="Generated by create next app"/>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
                <link rel="icon" href="/favicon.ico"/>
            </Head>
            <main className="container-sm">

                <div
                    className="container-sm d-sm-flex  justify-content-center align-items-center pt-5">
                    <div className="">
                        <Image
                            src="/img/ICON-LOGIN.jpg"
                            width={350}
                            height={350}
                            alt="contatoform"
                        />
                    </div>
                    <div className="">
                        <div className="mb-5">
                            <h2 className="">LOGIN</h2>
                            <h6>
                                Ainda não tem cadastro?
                                <Link
                                    href="/cadastro"
                                    className="link-primary link-underline-opacity-25 link-underline-opacity-100-hover"
                                >Clique aqui
                                </Link>
                                e cadastre-se.
                            </h6>
                        </div>

                        <div className="">
                            <input
                                type="usuário"
                                className="form-control  mb-4  border-primary"
                                id="login"
                                placeholder="Email"
                                {...register("login", {required: true})}
                            />
                            {errors?.login?.type === "required" && (
                                <p className=" text-danger fw-bold">Email Obrigatório!</p>
                            )}

                            <input
                                type="password"
                                className="form-control mb-4 border-primary"
                                id="senha"
                                placeholder="Senha"
                                {...register("senha", {required: true})}
                            />

                            {errors?.senha?.type === "required" && (
                                <p className=" text-danger fw-bold">Senha Obrigatório!</p>
                            )}

                            <button className="btn btn-primary w-100 mb-3" onClick={(e) => {
                                e.preventDefault()
                                handleSubmit(onSubmit)()}}>
                                Entrar
                            </button>
                            {erroLogin === true ? (
                                <p className="alert alert-danger">{erroLoginMessage}</p>) : ("")}
                        </div>
                    </div>
                </div>


            </main>
        </>
    );
}

export default Index;